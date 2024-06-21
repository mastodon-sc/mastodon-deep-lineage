package org.mastodon.mamut.classification.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificationTest
{
	@Test
	void testShowSuccess()
	{
		assertDoesNotThrow( () -> Notification.showSuccess( "Success", "This is a success message." ) );
	}
}
