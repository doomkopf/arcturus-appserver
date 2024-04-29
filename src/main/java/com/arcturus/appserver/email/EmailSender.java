package com.arcturus.appserver.email;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.ServerConfigPropery;

public class EmailSender
{
	private static final String CHARSET = "UTF-8";

	private final AWSStaticCredentialsProvider awsStaticCredentialsProvider;
	private final AmazonSimpleEmailService client;

	public EmailSender(Config config)
	{
		awsStaticCredentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(
			config.getString(ServerConfigPropery.awsSesAccessKey),
			config.getString(ServerConfigPropery.awsSesSecretKey)
		));

		client = AmazonSimpleEmailServiceClientBuilder.standard()
			.withRegion(Regions.fromName(config.getString(ServerConfigPropery.awsSesRegion)))
			.build();
	}

	public void send(String senderMail, String receiverMail, String subject, String body)
	{
		var request = new SendEmailRequest().withDestination(new Destination().withToAddresses(
			receiverMail))
			.withMessage(new Message().withBody(new Body().withText(new Content().withCharset(
				CHARSET).withData(body)))
				.withSubject(new Content().withCharset(CHARSET).withData(subject)))
			.withSource(senderMail);

		request.withRequestCredentialsProvider(awsStaticCredentialsProvider);

		client.sendEmail(request);
	}

	public void shutdown()
	{
		client.shutdown();
	}
}