package it.sara.demo.web.assembler;

import it.sara.demo.service.user.criteria.CriteriaUpdateUser;
import it.sara.demo.web.user.request.UpdateUserRequest;
import org.springframework.stereotype.Component;

@Component
public class UpdateUserAssembler {

    public CriteriaUpdateUser toCriteria(UpdateUserRequest request) {
        CriteriaUpdateUser criteria = new CriteriaUpdateUser();
        criteria.setGuid(request.getGuid());
        criteria.setFirstName(request.getFirstName());
        criteria.setLastName(request.getLastName());
        criteria.setEmail(request.getEmail());
        criteria.setPhoneNumber(request.getPhoneNumber());
        return criteria;
    }
}
