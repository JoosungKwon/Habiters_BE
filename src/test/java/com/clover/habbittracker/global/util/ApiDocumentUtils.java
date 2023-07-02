package com.clover.habbittracker.global.util;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;

public class ApiDocumentUtils {

	private ApiDocumentUtils() {
	}

	public static OperationRequestPreprocessor getDocumentRequest() {
		return preprocessRequest(modifyUris().scheme("https").host("habiters.api.com").removePort(), prettyPrint());
	}

	public static OperationResponsePreprocessor getDocumentResponse() {
		return preprocessResponse(prettyPrint());
	}
}
