package it.sara.demo.service.user.impl;

import it.sara.demo.dto.UserDTO;
import it.sara.demo.exception.GenericException;
import it.sara.demo.service.assembler.UserAssembler;
import it.sara.demo.service.database.UserRepository;
import it.sara.demo.service.database.model.User;
import it.sara.demo.service.user.criteria.CriteriaAddUser;
import it.sara.demo.service.user.criteria.CriteriaGetUsers;
import it.sara.demo.service.user.criteria.CriteriaUpdateUser;
import it.sara.demo.service.user.result.AddUserResult;
import it.sara.demo.service.user.result.GetUsersResult;
import it.sara.demo.service.user.result.UpdateUserResult;
import it.sara.demo.service.util.StringUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private StringUtil stringUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAssembler userAssembler;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Dato un criterio valido, l'aggiunta di un utente deve avere successo")
    void addUser_Success() throws GenericException {
        // Arrange
        CriteriaAddUser criteria = new CriteriaAddUser();
        criteria.setFirstName("Mario");
        criteria.setLastName("Rossi");
        criteria.setEmail("mario.rossi@example.it");
        criteria.setPhoneNumber("0212345678");

        when(stringUtil.isNullOrEmpty(any())).thenReturn(false);
        
        // Act
        AddUserResult result = userService.addUser(criteria);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Dato un database con utenti, il recupero lista deve restituire i DTO correttamente")
    void getUsers_WithData() throws GenericException {
        // Arrange
        CriteriaGetUsers criteria = new CriteriaGetUsers();
        criteria.setQuery("");
        criteria.setOffset(0);
        criteria.setLimit(20);
        criteria.setOrder(CriteriaGetUsers.OrderType.BY_FIRSTNAME);

        User user = new User();
        user.setFirstName("Mario");
        
        Page<User> userPage = new PageImpl<>(List.of(user));
        
        when(userRepository.searchUsers(eq(""), any(Pageable.class))).thenReturn(userPage);
        when(userAssembler.toDTO(any(User.class))).thenReturn(new UserDTO());

        // Act
        GetUsersResult result = userService.getUsers(criteria);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getUsers().size());
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("Dato un database vuoto, il recupero lista deve restituire una lista vuota")
    void getUsers_Empty() throws GenericException {
        // Arrange
        CriteriaGetUsers criteria = new CriteriaGetUsers();
        criteria.setQuery("");
        criteria.setOffset(0);
        criteria.setLimit(20);
        criteria.setOrder(CriteriaGetUsers.OrderType.BY_FIRSTNAME);

        when(userRepository.searchUsers(eq(""), any(Pageable.class))).thenReturn(Page.empty());

        // Act
        GetUsersResult result = userService.getUsers(criteria);

        // Assert
        assertNotNull(result);
        assertTrue(result.getUsers().isEmpty());
        assertEquals(0, result.getTotal());
    }

    @Test
    @DisplayName("Dato un GUID esistente, l'aggiornamento dell'utente deve avere successo")
    void updateUser_Success() throws GenericException {
        // Arrange
        CriteriaUpdateUser criteria = new CriteriaUpdateUser();
        criteria.setGuid("test-guid");
        criteria.setFirstName("Luigi");
        criteria.setLastName("Verdi");

        User existingUser = new User();
        existingUser.setGuid("test-guid");

        when(userRepository.findByGuid("test-guid")).thenReturn(Optional.of(existingUser));

        // Act
        UpdateUserResult result = userService.updateUser(criteria);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).save(existingUser);
        assertEquals("Luigi", existingUser.getFirstName());
    }

    @Test
    @DisplayName("Dato un GUID inesistente, l'aggiornamento deve lanciare GenericException 404")
    void updateUser_NotFound() {
        // Arrange
        CriteriaUpdateUser criteria = new CriteriaUpdateUser();
        criteria.setGuid("invalid-guid");

        when(userRepository.findByGuid("invalid-guid")).thenReturn(Optional.empty());

        // Act & Assert
        GenericException exception = assertThrows(GenericException.class, () -> userService.updateUser(criteria));
        assertEquals(404, exception.getStatus().getCode());
        assertEquals("User not found", exception.getStatus().getMessage());
    }
}
