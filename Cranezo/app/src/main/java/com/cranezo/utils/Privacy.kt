package com.cranezo.utils

/**
 * Privacy between the people on a job. A customer, an operator and an owner never see each other's phone number and never
 * call each other directly: the only number shown is the Cranezo toll-free support number (Help & Support, Emergency).
 * The code for the direct calls and the phone lines is still in place behind these two switches.
 * Set one to true to bring that part back.
 */
object Privacy {
    /** Call buttons that dial the other person on the job (customer <-> operator <-> owner) */
    const val PARTY_CALLS = false

    /** Phone numbers of the other people on a job (job details, receipt, owner details) */
    const val PARTY_PHONES = false
}
