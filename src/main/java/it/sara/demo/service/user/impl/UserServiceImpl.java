package it.sara.demo.service.user.impl;

import it.sara.demo.exception.GenericException;
import it.sara.demo.service.database.UserRepository;
import it.sara.demo.service.database.model.User;
import it.sara.demo.service.user.UserService;
import it.sara.demo.service.user.criteria.CriteriaAddUser;
import it.sara.demo.service.user.criteria.CriteriaGetUsers;
import it.sara.demo.service.user.criteria.CriteriaUpdateUser;
import it.sara.demo.service.user.result.AddUserResult;
import it.sara.demo.service.user.result.GetUsersResult;
import it.sara.demo.service.user.result.UpdateUserResult;
import it.sara.demo.service.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementazione della logica di business per la gestione degli utenti.
 * Gestisce creazione, ricerca paginata e aggiornamento, delegando la
 * persistenza a {@link UserRepository} e la mappatura agli assembler.
 *
 * @author Alina Valega
 * @version 1.0
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private StringUtil stringUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private it.sara.demo.service.assembler.UserAssembler userAssembler;

    /**
     * Aggiunge un nuovo utente dopo aver validato i campi obbligatori.
     * 
     * @param criteria Criteri contenenti i dati del nuovo utente
     * @return Risultato dell'operazione di inserimento
     * @throws GenericException in caso di campi mancanti o errori di sistema
     */
    @Override
    public AddUserResult addUser(CriteriaAddUser criteria) throws GenericException {

        AddUserResult returnValue;
        User user;

        try {

            returnValue = new AddUserResult();

            if (stringUtil.isNullOrEmpty(criteria.getFirstName())) {
                throw new GenericException(400, "First name is required");
            }
            if (stringUtil.isNullOrEmpty(criteria.getLastName())) {
                throw new GenericException(400, "Last name is required");
            }
            if (stringUtil.isNullOrEmpty(criteria.getEmail())) {
                throw new GenericException(400, "Email is required");
            }
            if (stringUtil.isNullOrEmpty(criteria.getPhoneNumber())) {
                throw new GenericException(400, "Phone is required");
            }

            user = new User();
            user.setGuid(UUID.randomUUID().toString());
            user.setFirstName(criteria.getFirstName());
            user.setLastName(criteria.getLastName());
            user.setEmail(criteria.getEmail());
            user.setPhoneNumber(criteria.getPhoneNumber());

            userRepository.save(user);

        } catch (GenericException e) {
            throw e;
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            throw new GenericException(GenericException.GENERIC_ERROR);
        }
        return returnValue;
    }

    /**
     * Recupera una lista paginata di utenti in base ai criteri di ricerca e ordinamento.
     * 
     * @param criteria Criteri di ricerca, paginazione e ordinamento
     * @return Risultato contenente la lista di DTO e il totale degli elementi
     * @throws GenericException in caso di errori durante l'interrogazione
     */
    @Override
    public GetUsersResult getUsers(CriteriaGetUsers criteria) throws GenericException {
        try {
            // Gestione del caso null (bug fix)
            if (criteria == null) {
                criteria = new CriteriaGetUsers();
                criteria.setQuery("");
                criteria.setOffset(0);
                criteria.setLimit(20);
                criteria.setOrder(CriteriaGetUsers.OrderType.BY_FIRSTNAME);
            }

            // Mappatura OrderType -> Sort
            Sort sort = switch (criteria.getOrder()) {
                case BY_FIRSTNAME_DESC -> Sort.by(Sort.Direction.DESC, "firstName");
                case BY_LASTNAME -> Sort.by(Sort.Direction.ASC, "lastName");
                case BY_LASTNAME_DESC -> Sort.by(Sort.Direction.DESC, "lastName");
                case BY_EMAIL -> Sort.by(Sort.Direction.ASC, "email");
                case BY_EMAIL_DESC -> Sort.by(Sort.Direction.DESC, "email");
                default -> Sort.by(Sort.Direction.ASC, "firstName");
            };

            Pageable pageable = PageRequest.of(criteria.getOffset(), criteria.getLimit(), sort);
            Page<User> userPage = userRepository.searchUsers(criteria.getQuery(), pageable);

            GetUsersResult result = new GetUsersResult();
            result.setUsers(userPage.getContent().stream().map(userAssembler::toDTO).toList());
            result.setTotal((int) userPage.getTotalElements());

            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new GenericException(GenericException.GENERIC_ERROR);
        }
    }

    /**
     * Aggiorna i dati di un utente esistente identificato dal suo GUID.
     * 
     * @param criteria Criteri contenenti il GUID e i nuovi dati anagrafici
     * @return Risultato dell'operazione di aggiornamento
     * @throws GenericException se l'utente non esiste o in caso di errore di sistema
     */
    @Override
    @Transactional
    public UpdateUserResult updateUser(CriteriaUpdateUser criteria) throws GenericException {
        try {
            // Ricerca utente sicura tramite GUID
            Optional<User> userOpt = userRepository.findByGuid(criteria.getGuid());

            if (userOpt.isEmpty()) {
                throw new GenericException(404, "User not found");
            }

            User user = userOpt.get();

            // Aggiornamento campi anagrafici
            user.setFirstName(criteria.getFirstName());
            user.setLastName(criteria.getLastName());
            user.setEmail(criteria.getEmail());
            user.setPhoneNumber(criteria.getPhoneNumber());

            // Salvataggio tramite repository (PreparedStatement interna previene SQL Injection)
            userRepository.save(user);

            return new UpdateUserResult();
        } catch (GenericException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new GenericException(GenericException.GENERIC_ERROR);
        }
    }
}
