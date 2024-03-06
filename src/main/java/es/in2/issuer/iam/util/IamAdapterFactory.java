package es.in2.issuer.iam.util;

import es.in2.issuer.iam.service.GenericIamAdapter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IamAdapterFactory {
    List<GenericIamAdapter> iamAdapters;

    public IamAdapterFactory(List<GenericIamAdapter> iamServices) {
        this.iamAdapters = iamServices;
    }

    public GenericIamAdapter getAdapter() {
        //check if list is empty or size is greater than 1
        if (iamAdapters.isEmpty() || iamAdapters.size() > 1) {
            throw new RuntimeException("Invalid number of config services found");
        }

        return iamAdapters.get(0);
    }
}
