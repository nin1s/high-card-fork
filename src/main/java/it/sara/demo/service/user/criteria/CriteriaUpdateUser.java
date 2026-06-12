package it.sara.demo.service.user.criteria;

import it.sara.demo.service.criteria.GenericCriteria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CriteriaUpdateUser extends GenericCriteria {
    private String guid;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
}
