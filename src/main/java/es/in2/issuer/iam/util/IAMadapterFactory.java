package es.in2.issuer.iam.util;

import es.in2.issuer.configuration.service.GenericConfigAdapter;
import es.in2.issuer.iam.service.GenericIAMadapter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IAMadapterFactory {
    List<GenericIAMadapter> iamAdapters;

    public IAMadapterFactory(List<GenericIAMadapter> iamServices) {
        this.iamAdapters = iamServices;
    }

    public GenericIAMadapter getAdapter() {
        //check if list is empty or size is greater than 1
        if (iamAdapters.isEmpty() || iamAdapters.size() > 1) {
            throw new RuntimeException("Invalid number of config services found");
        }

        return iamAdapters.get(0);
    }
}
