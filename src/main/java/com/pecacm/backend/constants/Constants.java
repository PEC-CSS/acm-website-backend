package com.pecacm.backend.constants;

public class Constants {
    public static final String UPDATE_SUCCESS = "Successfully Updated";

    /* Authorization Constants */
    //TODO: This is an ugly solution, need to refactor this to use values from the enum somehow
    public static final String HAS_ROLE_ADMIN = "hasRole('Admin')";
    public static final String HAS_ROLE_CORE = "hasRole('Core')";
    public static final String HAS_ROLE_EB = "hasRole('ExecutiveBody')";
    public static final String HAS_ROLE_IB = "hasRole('ImplementationBody')";
    public static final String HAS_ROLE_MEMBER = "hasRole('Member')";

}
