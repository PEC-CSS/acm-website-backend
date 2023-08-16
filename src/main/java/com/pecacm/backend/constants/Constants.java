package com.pecacm.backend.constants;

public class Constants {
    public static final String UPDATE_SUCCESS = "Successfully Updated";

    /* Authorization Constants */
    public static final String OR = " or ";
    public static final String HAS_ROLE_ADMIN = "hasRole('Admin')";
    public static final String HAS_ROLE_CORE = "hasRole('Core')";
    public static final String HAS_ROLE_EB = "hasRole('ExecutiveBody')";
    public static final String HAS_ROLE_IB = "hasRole('ImplementationBody')";
    public static final String HAS_ROLE_MEMBER = "hasRole('Member')";
    public static final String HAS_ROLE_ANONYMOUS = "hasRole('ANONYMOUS')";
    public static final String HAS_ROLE_CORE_AND_ABOVE = HAS_ROLE_ADMIN + OR + HAS_ROLE_CORE;
    public static final String HAS_ROLE_EB_AND_ABOVE = HAS_ROLE_CORE_AND_ABOVE + OR + HAS_ROLE_EB;
    public static final String HAS_ROLE_IB_AND_ABOVE = HAS_ROLE_EB_AND_ABOVE + OR + HAS_ROLE_IB;
    public static final String HAS_ROLE_MEMBER_AND_ABOVE = HAS_ROLE_IB_AND_ABOVE + OR + HAS_ROLE_MEMBER;
    public static final String HAS_ANY_ROLE = HAS_ROLE_MEMBER_AND_ABOVE + OR + HAS_ROLE_ANONYMOUS;
}
