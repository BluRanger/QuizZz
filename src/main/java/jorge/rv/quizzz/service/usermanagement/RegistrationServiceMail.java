package jorge.rv.quizzz.service.usermanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import jorge.rv.quizzz.model.MailRegistrationToken;
import jorge.rv.quizzz.model.TokenType;
import jorge.rv.quizzz.model.User;
import jorge.rv.quizzz.service.UserService;

@Service
@Profile("!test")
public class RegistrationServiceMail implements RegistrationService {

	private UserService userService;
	private TokenServiceMailRegistration tokenService;
	private TokenDeliverySystem tokenDeliveryService;
	
	@Autowired
	public RegistrationServiceMail(UserService userService, 
			TokenServiceMailRegistration tokenService, 
			TokenDeliverySystem tokenDeliveryService) {
		this.userService = userService;
		this.tokenService = tokenService;
		this.tokenDeliveryService = tokenDeliveryService;
	}

	@Override
	public void startRegistration(User user) {
		User newUser = userService.saveUser(user);
		
		MailRegistrationToken mailToken = tokenService.generateTokenForUser(newUser);
		
		tokenDeliveryService.sendTokenToUser(mailToken, newUser, TokenType.REGISTRATION_MAIL);
	}

	@Override
	public void continueRegistration(User user, String token) {
		tokenService.validateTokenForUser(user, token);
		
		userService.enableUser(user);
		tokenService.invalidateToken(token);
	}

	@Override
	public boolean isRegistrationCompleted(User user) {
		return userService.isUserEnabled(user);
	}

}