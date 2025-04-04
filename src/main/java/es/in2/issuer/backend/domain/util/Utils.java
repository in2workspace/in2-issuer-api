package es.in2.issuer.backend.domain.util;

import es.in2.issuer.backend.domain.exception.InvalidCredentialFormatException;
import es.in2.issuer.backend.domain.model.dto.credential.lear.LEARCredential;
import es.in2.issuer.backend.domain.model.dto.credential.lear.Mandator;
import es.in2.issuer.backend.domain.model.dto.credential.lear.Power;
import es.in2.issuer.backend.domain.model.dto.credential.lear.employee.LEARCredentialEmployee;
import es.in2.issuer.backend.domain.model.dto.credential.lear.machine.LEARCredentialMachine;

import java.util.List;


public class Utils {

    private Utils() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Power> extractPowers(LEARCredential credential) {
        List<String> types = credential.type();
        if (types.contains("LEARCredentialEmployee")) {
            return ((LEARCredentialEmployee) credential).credentialSubject().mandate().power();
        } else if (types.contains("LEARCredentialMachine")) {
            return ((LEARCredentialMachine) credential).credentialSubject().mandate().power();
        }
        throw new InvalidCredentialFormatException("Unsupported credential type: " + types);
    }

    public static Mandator extractMandator(LEARCredential credential) {
        List<String> types = credential.type();
        if (types.contains("LEARCredentialEmployee")) {
            return ((LEARCredentialEmployee) credential).credentialSubject().mandate().mandator();
        } else if (types.contains("LEARCredentialMachine")) {
            return ((LEARCredentialMachine) credential).credentialSubject().mandate().mandator();
        }
        throw new InvalidCredentialFormatException("Unsupported credential type: " + types);
    }
}
