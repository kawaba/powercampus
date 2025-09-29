package mailutil_service;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class JmClient {
	// REST クライアント

	private WebTarget webTarget;
	private Client client;
	//private static final String BASE_URI = "http://localhost:8080/mailservice/sendmail";
	private static final String BASE_URI = "http://localhost:8080/mailservice/sendmail";

	public JmClient() {
		client = ClientBuilder.newClient();
		webTarget = client.target(BASE_URI).path("cc");
	}

	public Response send(DataSet ds) throws ClientErrorException {
		return webTarget.path("text").request()
				.post(Entity.entity(ds, MediaType.APPLICATION_JSON));
	}

	public Response send_Sashikomi(DataSet ds) throws ClientErrorException {
		return webTarget.path("sashikomi").request()
				.post(Entity.entity(ds, MediaType.APPLICATION_JSON));
	}	
	
	public void close() {
		client.close();
	}
}