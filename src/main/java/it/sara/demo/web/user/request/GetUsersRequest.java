package it.sara.demo.web.user.request;

import it.sara.demo.web.request.GenericRequest;
import it.sara.demo.service.user.criteria.CriteriaGetUsers.OrderType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUsersRequest extends GenericRequest {

    private String search = "";
    private int page = 0;
    private int size = 20;
    private OrderType order = OrderType.BY_FIRSTNAME;

}
