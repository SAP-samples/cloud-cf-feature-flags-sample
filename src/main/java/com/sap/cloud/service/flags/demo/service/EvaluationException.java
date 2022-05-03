package com.sap.cloud.service.flags.demo.service;

/**
 * Exception class representing an error during the evaluation of a flag.
 */

public class EvaluationException extends RuntimeException {

	public EvaluationException(String message) {
		super(message);
	}

}
