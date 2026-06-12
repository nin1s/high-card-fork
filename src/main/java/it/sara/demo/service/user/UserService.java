package it.sara.demo.service.user;

import it.sara.demo.exception.GenericException;
import it.sara.demo.service.user.criteria.CriteriaAddUser;
import it.sara.demo.service.user.criteria.CriteriaGetUsers;
import it.sara.demo.service.user.criteria.CriteriaUpdateUser;
import it.sara.demo.service.user.result.AddUserResult;
import it.sara.demo.service.user.result.GetUsersResult;
import it.sara.demo.service.user.result.UpdateUserResult;

public interface UserService {

    AddUserResult addUser(CriteriaAddUser addUserRequest) throws GenericException;

    GetUsersResult getUsers(CriteriaGetUsers criteriaGetUsers) throws GenericException;

    UpdateUserResult updateUser(CriteriaUpdateUser criteriaUpdateUser) throws GenericException;
}

