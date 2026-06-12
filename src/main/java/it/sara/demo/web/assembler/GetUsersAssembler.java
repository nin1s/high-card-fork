package it.sara.demo.web.assembler;

import it.sara.demo.service.user.criteria.CriteriaGetUsers;
import it.sara.demo.web.user.request.GetUsersRequest;
import org.springframework.stereotype.Component;

@Component
public class GetUsersAssembler {

    public CriteriaGetUsers toCriteria(GetUsersRequest request) {
        CriteriaGetUsers criteria = new CriteriaGetUsers();
        
        // Gestione null e default per search
        criteria.setQuery(request.getSearch() == null ? "" : request.getSearch());
        
        // Gestione page < 0 -> 0
        criteria.setOffset(Math.max(0, request.getPage()));
        
        // Gestione size <= 0 -> 20
        criteria.setLimit(request.getSize() <= 0 ? 20 : request.getSize());
        
        // L'ordine è già tipizzato dall'enum
        criteria.setOrder(request.getOrder());
        
        return criteria;
    }
}
