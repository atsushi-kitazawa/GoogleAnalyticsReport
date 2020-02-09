package com.example.google;

import java.io.IOException;

import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;

/**
 * @author atsushi.kitazawa
 */
public interface ResponseOutput {

	public void parse(GetReportsResponse response);

	public void output() throws IOException;
}
