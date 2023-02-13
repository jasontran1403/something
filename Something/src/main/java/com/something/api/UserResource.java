package com.something.api;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import com.something.domain.Activation;
import com.something.domain.Affiliate;
import com.something.domain.CashWallet;
import com.something.domain.CommissionWallet;
import com.something.domain.Email;
import com.something.domain.HistoryWallet;
import com.something.domain.Investment;
import com.something.domain.Pack;
import com.something.domain.Role;
import com.something.domain.User;
import com.something.dto.UserDTO;
import com.something.service.ActivationService;
import com.something.service.AffiliateService;
import com.something.service.AuthenticatorService;
import com.something.service.CashWalletService;
import com.something.service.CommissionWalletService;
import com.something.service.HistoryWalletService;
import com.something.service.InvestmentService;
import com.something.service.MaillerService;
import com.something.service.PackService;
import com.something.service.UserService;
import com.something.totp.TotpAutoConfiguration;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserResource {
	@Autowired
	ActivationService actiService;

	@Autowired
	PackService packService;

	@Autowired
	CommissionWalletService cmwService;

	@Autowired
	UserService userService;

	@Autowired
	MaillerService mailerServie;

	@Autowired
	AffiliateService affService;

	@Autowired
	CashWalletService cwService;

	@Autowired
	HistoryWalletService hwService;

	@Autowired
	InvestmentService investService;

	@Autowired
	AuthenticatorService authService;

	@Autowired
	TotpAutoConfiguration verifyCode;

	@Autowired
	private SecretGenerator secretGenerator;

	@Autowired
	private QrDataFactory qrDataFactory;

	@Autowired
	private QrGenerator qrGenerator;

	@GetMapping("/authentication/showQR/{username}")
	public List<String> generate2FA(@PathVariable("username") String username)
			throws QrGenerationException, WriterException, IOException, CodeGenerationException {
		User user = userService.getUser(username);
		QrData data = qrDataFactory.newBuilder().label(user.getUsername()).secret(user.getSecret())
				.issuer("Something Application").period(30).build();

		String qrCodeImage = getDataUriForImage(qrGenerator.generate(data), qrGenerator.getImageMimeType());
		List<String> info2FA = new ArrayList<>();
		String isEnabled = "";
		if (user.isMfaEnabled()) {
			isEnabled = "true";
		} else {
			isEnabled = "false";
		}
		info2FA.add(isEnabled);
		info2FA.add(user.getSecret());
		info2FA.add(qrCodeImage);

		return info2FA;
	}

	@PostMapping("/authentication/enabled")
	public String enabled(@RequestParam("username") String username, @RequestParam("code") String code) {
		User user = userService.getUser(username);
		TimeProvider timeProvider = new SystemTimeProvider();
		CodeGenerator codeGenerator = new DefaultCodeGenerator();
		DefaultCodeVerifier verify = new DefaultCodeVerifier(codeGenerator, timeProvider);
		verify.setAllowedTimePeriodDiscrepancy(0);

		if (verify.isValidCode(user.getSecret(), code)) {
			userService.enabledAuthen(user);
			return "Enabled Success";
		} else {
			return "Enabled Failed";
		}
	}

	@PostMapping("/user/changePassword")
	public String changePassword(@RequestParam("username") String username,
			@RequestParam("currentPassword") String currentPassword, @RequestParam("newPassword") String newPassword,
			@RequestParam("confirmNewPassword") String confirmNewPassword, @RequestParam("authen") String authen) {
		User user = userService.getUser(username);
		TimeProvider timeProvider = new SystemTimeProvider();
		CodeGenerator codeGenerator = new DefaultCodeGenerator();
		DefaultCodeVerifier verify = new DefaultCodeVerifier(codeGenerator, timeProvider);
		verify.setAllowedTimePeriodDiscrepancy(0);

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		if (encoder.matches(currentPassword, user.getPassword())) {
			if (user.isMfaEnabled()) {
				if (verify.isValidCode(user.getSecret(), authen)) {
					user.setPassword(encoder.encode(newPassword));
					userService.changePassword(user);
					return "Change password success";
				} else {
					return "2FA code is incorrect";
				}
			} else {
				user.setPassword(encoder.encode(newPassword));
				userService.changePassword(user);
				return "Change password success";
			}
		} else {
			return "Old password is incorrect";
		}
	}

	@PostMapping("/authentication/disabled")
	public String disabled(@RequestParam("username") String username, @RequestParam("code") String code) {
		User user = userService.getUser(username);
		TimeProvider timeProvider = new SystemTimeProvider();
		CodeGenerator codeGenerator = new DefaultCodeGenerator();
		DefaultCodeVerifier verify = new DefaultCodeVerifier(codeGenerator, timeProvider);
		verify.setAllowedTimePeriodDiscrepancy(0);

		if (verify.isValidCode(user.getSecret(), code)) {
			userService.disabledAuthen(user);
			return "Disabled Success";
		} else {
			return "Disabled Failed";
		}

	}

	@GetMapping("/getAllData/users")
	public ResponseEntity<List<User>> getUsers() {
		return ResponseEntity.ok().body(userService.getUsers());
	}

	@GetMapping("/user/{username}")
	public ResponseEntity<UserDTO> getUserInfo(@PathVariable("username") String username) {
		UserDTO userDTO = new UserDTO();
		User user = userService.getUser(username);
		userDTO.setUser(user);
		CashWallet cw = cwService.findByUsername(username);
		CommissionWallet cmw = cmwService.findByUsername(username);

		userDTO.setCashbalance(cw.getBalance());
		userDTO.setCommissionbalance(cmw.getBalance());

		if (user.getLeftref().equals("")) {
			userDTO.setLeftrefsale(0);
		} else {
			userDTO.setLeftrefsale(userService.getUser(user.getLeftref()).getTeamsales());
		}
		if (user.getRightref().equals("")) {
			userDTO.setRightrefsale(0);
		} else {
			userDTO.setRightrefsale(userService.getUser(user.getLeftref()).getTeamsales());
		}

		return ResponseEntity.ok().body(userDTO);
	}

	@PostMapping("/user/validation")
	public ResponseEntity<String> getUserIsActivated(@RequestParam("username") String username,
			@RequestParam("password") String password, @RequestParam("authen") String authen) {
		User user = userService.getUser(username);
		try {
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

			if (user.isActived() == false) {
				if (encoder.matches(password, user.getPassword())) {
					return ResponseEntity.ok().body("Not Actived");
				} else {
					return ResponseEntity.ok().body("Password is not correct");
				}
			} else {
				if (encoder.matches(password, user.getPassword())) {
					if (user.isMfaEnabled()) {
						TimeProvider timeProvider = new SystemTimeProvider();
						CodeGenerator codeGenerator = new DefaultCodeGenerator();
						DefaultCodeVerifier verify = new DefaultCodeVerifier(codeGenerator, timeProvider);
						verify.setAllowedTimePeriodDiscrepancy(0);
						if (verify.isValidCode(user.getSecret(), authen)) {
							return ResponseEntity.ok().body("success");
						} else {
							return ResponseEntity.ok().body("Wrong 2FA");
						}
					} else {
						return ResponseEntity.ok().body("success");
					}

				} else {
					return ResponseEntity.ok().body("Password is not correct");
				}
			}
		} catch (Exception e) {
			return ResponseEntity.ok().body("Username is not exist");
		}
	}

	@PostMapping("/affiliate/generate")
	public ResponseEntity<Affiliate> addAffiliate(@RequestParam("root") String root,
			@RequestParam("placement") String placement, @RequestParam("side") String side) {
		return ResponseEntity.ok().body(affService.addRegisURL(root, placement, side));
	}

	@GetMapping("/packages")
	public ResponseEntity<List<Pack>> getAllPackages() {
		return ResponseEntity.ok().body(packService.getAllPackges());
	}

	@GetMapping("/investment/withdrawCapital/{investmentcode}")
	public ResponseEntity<Investment> withdrawCapital(@PathVariable("investmentcode") String investmentcode) {
		return ResponseEntity.ok().body(investService.withdrawCapital(investmentcode));
	}

	@GetMapping("/cashWallet/balance/{username}")
	public ResponseEntity<CashWallet> getCashWalletBalance(@PathVariable("username") String username) {
		return ResponseEntity.ok().body(cwService.findByUsername(username));
	}

	@GetMapping("/commissionWallet/balance/{username}")
	public ResponseEntity<CommissionWallet> getCommissionWalletBalance(@PathVariable("username") String username) {
		return ResponseEntity.ok().body(cmwService.findByUsername(username));
	}

	@GetMapping("/history/commission/{username}")
	public ResponseEntity<List<HistoryWallet>> getAllCommissionHistories(@PathVariable("username") String username) {
		return ResponseEntity.ok().body(hwService.findCommissionHistoryByUsername(username));
	}

	@GetMapping("/history/swap/{username}")
	public ResponseEntity<List<HistoryWallet>> getAllSwapHistories(@PathVariable("username") String username) {
		return ResponseEntity.ok().body(hwService.findSwapHistoryByUsername(username));
	}

	@GetMapping("/history/withdraw/{username}")
	public ResponseEntity<List<HistoryWallet>> getAllWithdrawHistories(@PathVariable("username") String username) {
		return ResponseEntity.ok().body(hwService.findWithdrawHistoryByUsername(username));
	}

	@GetMapping("/history/deposit/{username}")
	public ResponseEntity<List<HistoryWallet>> getAllDepositHistories(@PathVariable("username") String username) {
		return ResponseEntity.ok().body(hwService.findDepositHistoryByUsername(username));
	}

	@GetMapping("/history/transfer/{username}")
	public ResponseEntity<List<HistoryWallet>> getAllTransferHistories(@PathVariable("username") String username) {
		return ResponseEntity.ok().body(hwService.findTransferHistoryByUsername(username));
	}

	@GetMapping("/history/runningInvestment/{username}")
	public ResponseEntity<List<Investment>> getAllInvestmentRunningHistories(
			@PathVariable("username") String username) {
		return ResponseEntity.ok().body(investService.getAllActiveByUsername(username));
	}

	@GetMapping("/history/investment/{username}")
	public ResponseEntity<List<Investment>> getAllInvestmentHistories(@PathVariable("username") String username) {
		return ResponseEntity.ok().body(investService.getAllByUsername(username));
	}

	@PostMapping("/wallet/swap")
	public ResponseEntity<String> swapCommission(@RequestParam("username") String username,
			@RequestParam("amount") double amount) {
		User user = userService.getUser(username);
		if (user.isLocked()) {
			return ResponseEntity.ok().body("Your account is locked trade method, please contact to customer service");
		}
		CommissionWallet cmw = cmwService.findByUsername(username);
		CashWallet cw = cwService.findByUsername(username);
		if (cmw.getBalance() < amount) {
			return ResponseEntity.ok().body("Amount is greater than commisison balance");
		}
		if (user.getMaxoutleft() < amount) {
			return ResponseEntity.ok().body("Amount is greater than Max out");
		}
		cmw.setBalance(cmw.getBalance() - amount);
		cmwService.updateBalance(cmw);

		cw.setBalance(cw.getBalance() + amount);
		cwService.updateBalance(cw);

		userService.updateMaxOut(user, amount, "swap");

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
		LocalDateTime dateTime = LocalDateTime.now();
		String formattedDateTime = dateTime.format(formatter);
		String uuid = UUID.randomUUID().toString();

		HistoryWallet hw = new HistoryWallet();
		hw.setAmount(amount);
		hw.setTime(formattedDateTime);
		hw.setType("Swap commission");
		hw.setCode(uuid);
		hw.setHash("");
		hw.setStatus("success");
		hw.setFrominvestment("");
		hw.setUsername(username);
		hwService.update(hw);

		return ResponseEntity.ok().body("success");
	}

	@PostMapping("/wallet/deposit")
	public ResponseEntity<String> deposit(@RequestParam("username") String username,
			@RequestParam("amount") double amount) {
		System.out.println(username);
		System.out.println(amount);
		return ResponseEntity.ok().body("OK");
	}

	@PostMapping("/wallet/transfer")
	public ResponseEntity<String> transfer(@RequestParam("username") String username,
			@RequestParam("receiver") String receiver, @RequestParam("amount") double amount) {
		User user = userService.getUser(username);
		if (user.isLocked()) {
			return ResponseEntity.ok().body("Your account is locked trade method, please contact to customer service");
		}

		CommissionWallet cw = cmwService.findByUsername(username);

		User receiverUser = userService.getUser(receiver);
		CommissionWallet receiverCW = cmwService.findByUsername(receiver);

		if (cw.getBalance() < amount) {
			return ResponseEntity.ok().body("Amount is greater than your balance");
		}
		if (user.getMaxoutleft() < amount) {
			return ResponseEntity.ok().body("Amount is greater than your Max out");
		}

		cw.setBalance(cw.getBalance() - amount);
		cmwService.updateBalance(cw);

		receiverCW.setBalance(receiverCW.getBalance() + amount);
		cmwService.updateBalance(receiverCW);

		userService.updateMaxOut(user, amount, "transfer");
		userService.updateMaxOut(receiverUser, amount, "receive");

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
		LocalDateTime dateTime = LocalDateTime.now();
		String formattedDateTime = dateTime.format(formatter);
		String uuid = UUID.randomUUID().toString();

		HistoryWallet hw = new HistoryWallet();
		hw.setAmount(amount);
		hw.setTime(formattedDateTime);
		hw.setType("Transfer");
		hw.setCode(uuid);
		hw.setHash("");
		hw.setStatus("success");
		hw.setCashfrom(username);
		hw.setCashto(receiver);
		hw.setFrominvestment("");
		hw.setUsername(username);
		hwService.update(hw);

		return ResponseEntity.ok().body("success");
	}

	@PostMapping("/wallet/withdraw")
	public ResponseEntity<String> withdraw(@RequestParam("walletaddress") String walletaddress,
			@RequestParam("amount") double amount) {
		User user = new User();
		if (user.isLocked()) {
			return ResponseEntity.ok().body("Your account is locked trade method, please contact to customer service");
		}
		System.out.println(walletaddress);
		System.out.println(amount);
		return ResponseEntity.ok().body("OK");
	}

	@PostMapping("/package/buy")
	public ResponseEntity<String> buyPackage(@RequestParam("packid") int packid,
			@RequestParam("username") String username) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
		LocalDateTime dateTime = LocalDateTime.now();
		String formattedDateTime = dateTime.format(formatter);
		String randomString = UUID.randomUUID().toString();
		String uuid = randomString.substring(24, randomString.length());
		String forDirect = UUID.randomUUID().toString();
		String forSelf = UUID.randomUUID().toString();
		Pack pack = packService.findById(packid);
		User user = userService.getUser(username);
		CashWallet cw = cwService.findByUsername(username);
		HistoryWallet hw = new HistoryWallet();
		Investment invest = new Investment();
		invest.setPackageId(packid);
		invest.setUsername(username);
		invest.setTime(formattedDateTime);
		invest.setCapital(pack.getPrice());
		invest.setCode(uuid);
		invest.setCapital(pack.getPrice());
		invest.setCount(0);
		invest.setClaimable(0);
		invest.setRemain(pack.getPrice());
		boolean check = false;
		if (pack.getPrice() <= cw.getBalance()) {
			check = true;
		}

		if (check) {
			investService.save(invest);
			userService.updateSale(user.getUsername(), pack.getPrice());
			userService.updateMaxOut(user, pack.getPrice(), "buy");

			User sponsor = userService.getUser(user.getRootUsername());
			CommissionWallet cmwSponsor = cmwService.findByUsername(sponsor.getUsername());
			HistoryWallet hwSponsor = new HistoryWallet();

			double commissionrate = 0;
			int rank = sponsor.getRank();

			switch (rank) {
			case 1:
				commissionrate = 5;
				break;
			case 2:
				commissionrate = 5.5;
				break;
			case 3:
				commissionrate = 6;
				break;
			case 4:
				commissionrate = 7;
				break;
			case 5:
				commissionrate = 8;
				break;
			case 6:
				commissionrate = 9;
				break;
			case 7:
				commissionrate = 10;
				break;
			case 8:
				commissionrate = 12;
				break;
			case 9:
				commissionrate = 15;
				break;
			}

			if (sponsor.getRank() > 0) {
				double directCommission = cmwSponsor.getBalance() + (pack.getPrice() * commissionrate / 100);

				cmwSponsor.setBalance(directCommission);

				hwSponsor.setAmount(pack.getPrice() * commissionrate / 100);
				hwSponsor.setCashfrom(user.getUsername());
				hwSponsor.setCashto(sponsor.getUsername());
				hwSponsor.setFrominvestment(uuid);
				hwSponsor.setTime(formattedDateTime);
				hwSponsor.setType("Direct Commission");
				hwSponsor.setCode(forDirect);
				hwSponsor.setHash("");
				hwSponsor.setStatus("success");
				hwSponsor.setUsername(cmwSponsor.getUsername());
				hwService.update(hwSponsor);
				cmwService.updateBalance(cmwSponsor);

			}

			cw.setBalance(cw.getBalance() - pack.getPrice());
			hw.setAmount(pack.getPrice());
			hw.setTime(formattedDateTime);
			hw.setCode(forSelf);
			hw.setHash("");
			hw.setStatus("success");
			hw.setType("Buy Package");
			hw.setUsername(cw.getUsername());
			hwService.update(hw);
			cwService.updateBalance(cw);

			List<User> listUser = userService.getTreeUpToRoot(username);

			for (User item : listUser) {
				if (item.getRank() != 0) {
					userService.updateteamSale(item.getUsername(), pack.getPrice());
				} else {
					continue;
				}
			}

			userService.calRank();
			return ResponseEntity.ok().body("OK");
		} else {
			return ResponseEntity.ok().body("Failed, balance is not enough to buy this package");
		}
	}

	@GetMapping("/affiliate/getByRoot/investment")
	public ResponseEntity<List<Investment>> getAll() {
		return ResponseEntity.ok().body(investService.getAllInvestment());
	}

	// Lấy reflink từ {username}
	@GetMapping("/affiliate/getByRoot/{root}")
	public ResponseEntity<List<Affiliate>> getAffiliateByRoot(@PathVariable("root") String root) {
		return ResponseEntity.ok().body(affService.getByRoot(root));
	}

	// Lấy reflink từ {username}
	@GetMapping("/affiliate/getByPlacement/{placement}")
	public ResponseEntity<List<Affiliate>> getAffiliateByPlacement(@PathVariable("placement") String placement) {
		return ResponseEntity.ok().body(affService.getByPlacement(placement));
	}

	// Lấy reflink bên trái/phải
	@GetMapping("/affiliate/getByPlacement/{placement}/{side}")
	public ResponseEntity<Affiliate> getAffiliateByPlacementAndSide(@PathVariable("placement") String placement,
			@PathVariable("side") String side) {
		return ResponseEntity.ok().body(affService.getByPlacementAndSide(placement, side));
	}

	// Lấy thông tin ref đăng ký
	@GetMapping("/affiliate/{uuid}")
	public ResponseEntity<Affiliate> getAffiliate(@PathVariable String uuid) {
		return ResponseEntity.ok().body(affService.getByUUID(uuid));
	}

	// lấy 15 tầng phía trên
	@GetMapping("/userTreeUp/{username}")
	public ResponseEntity<List<User>> getUserUp(@PathVariable String username) {
		return ResponseEntity.ok().body(userService.getTreeUp(username));
	}

	// lấy lên đến root
	@GetMapping("/userTreeUpToRoot/{username}")
	public ResponseEntity<List<User>> getUserUpToRoot(@PathVariable String username) {
		return ResponseEntity.ok().body(userService.getTreeUpToRoot(username));
	}

	// lấy 15 tầng xuống
	@GetMapping("/userMapDown/{username}")
	public ResponseEntity<HashMap<String, List<User>>> getMapDown(@PathVariable String username) {
		return ResponseEntity.ok().body(userService.getMapDown(username));
	}

	// lấy 5 tầng xuống
	@GetMapping("/userMapDown5Level/{username}")
	public ResponseEntity<HashMap<String, List<User>>> getMapDown5Level(@PathVariable String username) {
		return ResponseEntity.ok().body(userService.getMapDown5Level(username));
	}

	@PutMapping("/affiliate/{uuid}")
	public ResponseEntity<String> updateAffiliate(@PathVariable String uuid) {
		affService.updateRegistered(uuid);
		return ResponseEntity.ok().body("OK");
	}

	@PostMapping("/user/regis")
	public ResponseEntity<User> saveUser(@RequestBody User user) {
		URI uri = URI
				.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user/regis").toUriString());

		int checkPinNumber = (int) Math.floor(((Math.random() * 899999) + 100000));
		String uuid = UUID.randomUUID().toString();
		Activation acti = new Activation();
		acti.setUuid(uuid);
		acti.setUsername(user.getUsername());
		acti.setActivation(checkPinNumber);
		actiService.save(acti);

		Thread thread = new Thread() {
			public void run() {
				sendMail(user.getEmail(), "Link: http://localhost:3000/active-account/" + acti.getUuid() + "\n"
						+ "Active code: " + checkPinNumber);
			}
		};
		thread.start();

		CommissionWallet cmw = new CommissionWallet();
		cmw.setBalance(0);
		cmw.setUsername(user.getUsername());
		cmwService.createCommissionWallet(cmw);

		CashWallet cw = new CashWallet();
		cw.setBalance(0);
		cw.setUsername(user.getUsername());
		cwService.createCashWallet(cw);

		String secret = secretGenerator.generate();

		user.setSecret(secret);

		return ResponseEntity.created(uri).body(userService.regis(user));
	}

	@PostMapping("/user/active/{uuid}")
	public ResponseEntity<String> active(@PathVariable("uuid") String uuid,
			@RequestParam("activecode") String activecode) {
		Activation acti = actiService.getActivation(uuid);
		if (acti.getActivation() == Integer.parseInt(activecode)) {
			User user = userService.getUser(acti.getUsername());
			userService.activated(user);

			actiService.activated(acti);
			return ResponseEntity.ok().body("Activation success");
		} else {
			return ResponseEntity.ok().body("Wrong activation code");
		}
	}

	@PostMapping("/user/active/resend")
	public ResponseEntity<String> resendactive(@RequestParam("username") String username) {
		Activation acti = actiService.getActivationByUsername(username);
		User user = userService.getUser(username);

		if (acti == null) {
			return ResponseEntity.ok().body("Cannot find your usename, please try again");
		} else {
			int checkPinNumber = (int) Math.floor(((Math.random() * 899999) + 100000));

			Thread thread = new Thread() {
				public void run() {
					sendMail(user.getEmail(), "Link: http://localhost:3000/active-account/" + acti.getUuid() + "\n"
							+ "Active code: " + checkPinNumber);
				}
			};

			thread.start();

			actiService.reGenerateActi(acti, checkPinNumber);

			return ResponseEntity.ok().body(acti.getUuid());
		}
	}

	@PutMapping("/user/updateRef")
	public ResponseEntity<User> saveUser(@RequestParam String username, @RequestParam String usernameRef,
			@RequestParam String side) {
		URI uri = URI
				.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user/updateRef").toUriString());
		return ResponseEntity.created(uri).body(userService.updateRef(username, usernameRef, side));
	}

	@PostMapping("/role/save")
	public ResponseEntity<Role> saveRole(@RequestBody Role role) {
		URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/role/save").toUriString());
		return ResponseEntity.created(uri).body(userService.saveRole(role));
	}

	@GetMapping("/token/refresh")
	public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String authorizationHeader = request.getHeader(AUTHORIZATION);
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			try {
				String refresh_token = authorizationHeader.substring("Bearer ".length());
				Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
				JWTVerifier verifier = JWT.require(algorithm).build();
				DecodedJWT decodedJWT = verifier.verify(refresh_token);
				String username = decodedJWT.getSubject();
				User user = userService.getUser(username);
				String access_token = JWT.create().withSubject(user.getUsername())
						.withExpiresAt(new Date(System.currentTimeMillis() + 30 * 60 * 1000))
						.withIssuer(request.getRequestURL().toString())
						.withClaim("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
						.sign(algorithm);
				Map<String, String> tokens = new HashMap<>();
				tokens.put("access_token", access_token);
				tokens.put("refresh_token", refresh_token);
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				new ObjectMapper().writeValue(response.getOutputStream(), tokens);
			} catch (Exception exception) {
				response.setHeader("error", exception.getMessage());
				response.setStatus(FORBIDDEN.value());
				Map<String, String> error = new HashMap<>();
				error.put("error_message", exception.getMessage());
				response.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
				new ObjectMapper().writeValue(response.getOutputStream(), error);
			}
		} else {
			throw new RuntimeException("Refresh token is missing");
		}
	}

	public void sendMail(String emailTo, String body) {
		Email m = new Email();
		m.setFrom("test@hotmail.com");
		m.setSubject("hehehe ");
		m.setTo(emailTo);
		m.setBody(body);
		try {
			mailerServie.send(m);
		} catch (Exception e) {
			System.out.println("Error : " + e.getMessage());
		}
	}
}
