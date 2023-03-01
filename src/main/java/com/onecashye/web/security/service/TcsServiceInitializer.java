package com.onecashye.web.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Service
@PropertySource(ignoreResourceNotFound = true, value = "classpath:tcs.properties")
public class TcsServiceInitializer {

	private final String TcsHost;
	private final String TcsAuthFunctionName;
	private final String TcsAllowedTerminalType;
	private final Integer TcsCallTimeout;
	
	private final String TcsProxyUser;
	private final String TcsProxyPassword;
	
	private final String TcsFunctionAuthenticate;
	private final String TcsFunctionAccountExists;
	private final String TcsFunctionGetAccountInfoIsoByAccount;
	private final String TcsFunctionGetAccountInfoIsoByAlias;
	private final String TcsFunctionViewAccountType;
	private final String TcsFunctionGetMyAccountInfo;
	private final String TcsFunctionRequestTac;
	private final String TcsFunctionValidateTac;
	private final String TcsFunctionChangePassword;
	
	private final String TcsFunctionPayment;
	private final String TcsFunctionRedeem;
	private final String TcsFunctionBillPay;
	private final String TcsFunctionRefund;
	private final String TcsFunctionSalesRequest;
	private final String TcsFunctionSalesRequestCheck;
	private final String TcsFunctionSalesRequestExec;
	private final String TcsFunctionProxyTransaction;
	
	private final String TcsFunctionGetPrivileges;
	private final String TcsFunctionSignupUser;
	private final String TcsFunctionChangeUserStatus;
	private final String TcsFunctionAddStaff;
	private final String TcsFunctionDeleteStaff;
	private final String TcsFunctionResetStaff;
	private final String TcsFunctionVerifyAccountLevel;
	private final String TcsFunctionCreateAccountNoTac;
	private final String TwoFactorAuthEnabled;
	private final String TcsFunctionBalanceMWallet;
	private final String TcsFunctionBalanceProxyOwner;
	private final String TcsFunctionChangeLanguage;
	private final String TcsFunctionSetAlias;
	private final String TcsFunctionSendVoucher;
	private final String TcsFunctionSendToBank;
	private final String TcsFunctionRequestRefund;
	private final String TcsFunctionRequestRefundApproval;
	private final String TcsFunctionResetMyUser;
	private final String PgHost;
	private final String PgFunctionInquiry;
	private final String TCSFunctionFilteredCorePrivilegs;
	private final String TCSFunctionRegisterSubscriber;
	private final String TCSFunctionUpdateSubscriberParam39;
	private final String TCSFunctionBCashOutSalesRequest;
	private final String TCSFunctionForex;
	private final String TCSFunctionGetUserCorePrivileges;
	

	@Autowired
	TcsServiceInitializer(@Value("${tcs.host.url}") String TcsHost,
			@Value("${tcs.auth.function}") String TcsAuthFunctionName,
			@Value("${tcs.terminal.type}") String TcsAllowedTerminalType,
			@Value("${tcs.http.timeout}") Integer TcsCallTimeout,
			@Value("${tcs.proxy.user}") String TcsProxyUser,
			@Value("${tcs.proxy.password}") String TcsProxyPassword,
			@Value("${tcs.function.authenticate.body}") String TcsFunctionAuthenticate, 
			@Value("${tcs.function.accountexists.body}") String tcsFunctionAccountExists, 
			@Value("${tcs.function.getaccountinfoiso.byaccount.body}") String tcsFunctionGetAccountInfoIsoByAccount, 
			@Value("${tcs.function.getaccountinfoiso.byalias.body}") String tcsFunctionGetAccountInfoIsoByAlias, 
			@Value("${tcs.function.viewaccounttype.body}") String tcsFunctionViewAccountType,
			@Value("${tcs.function.getmyaccountinfo.body}") String tcsFunctionGetMyAccountInfo, 
			@Value("${tcs.function.requesttac.body}") String tcsFunctionRequestTac, 
			@Value("${tcs.function.validatetac.body}") String tcsFunctionValidateTac, 
			@Value("${tcs.function.changepassword.body}") String tcsFunctionChangePassword, 
			@Value("${tcs.function.payment.body}") String tcsFunctionPayment, 
			@Value("${tcs.function.redeem.body}") String tcsFunctionRedeem, 
			@Value("${tcs.function.bill.body}") String tcsFunctionBillPay, 
			@Value("${tcs.function.refund.body}") String tcsFunctionRefund, 
			@Value("${tcs.function.sales.request.body}") String tcsFunctionSalesRequest, 
			@Value("${tcs.function.sales.request.check.body}") String tcsFunctionSalesRequestCheck, 
			@Value("${tcs.function.sales.request.exec.body}") String tcsFunctionSalesRequestExec, 
			@Value("${tcs.function.getcoreprivileges.body}") String tcsFunctionGetPrivileges, 
			@Value("${tcs.function.trx.proxy.body}") String tcsFunctionProxyTransaction, 
			@Value("${tcs.function.signupuser.body}") String tcsFunctionSignupUser, 
			@Value("${tcs.function.changeuserstatus.body}") String tcsFunctionChangeUserStatus, 
			@Value("${tcs.function.addstaff.body}") String tcsFunctionAddStaff, 
			@Value("${tcs.function.resetstaff.body}") String tcsFunctionResetStaff, 
			@Value("${tcs.function.deletestaff.body}") String tcsFunctionDeleteStaff, 
			@Value("${tcs.function.verifyaccountlevel.body}") String tcsFunctionVerifyAccountLevel, 
			@Value("${tcs.function.createaccountnotac.body}") String tcsFunctionCreateAccountNoTac, 
			@Value("${2fa.enabled}") String twoFactorAuthEnabled, 
			@Value("${tcs.function.balancemwallet.body}") String tcsFunctionBalanceMWallet, 
			@Value("${tcs.function.balanceproxyowner.body}") String tcsFunctionBalanceProxyOwner, 
			@Value("${tcs.function.changelanguage.body}") String tcsFunctionChangeLanguage, 
			@Value("${tcs.function.setalias.body}") String tcsFunctionSetAlias, 
			@Value("${tcs.function.send.voucher.body}") String tcsFunctionSendVoucher, 
			@Value("${tcs.function.bank.body}") String tcsFunctionSendToBank, 
			@Value("${tcs.function.request.refund.body}") String tcsFunctionRequestRefund, 
			@Value("${tcs.function.request.refund.approval.body}") String tcsFunctionRequestRefundApproval, 
			@Value("${tcs.function.resetmyuserpassword.body}") String tcsFunctionResetMyUser, 
			@Value("${pg.host.url}") String pgHost, 
			@Value("${pg.function.inquiry}") String pgFunctionInquiry, 
			@Value("${tcs.function.getcoreprivileges.filter.body}") String tcsFunctionFilteredCorePrivilegs, 
			@Value("${tcs.function.register.subscriber.body}") String tcsFunctionRegisterSubscriber, 
			@Value("${tcs.function.update.subscriber.docs.body}") String tcsFunctionUpdateSubscriberParam39, 
			@Value("${tcs.function.bzcashout.sales.request.body}") String tcsFunctionBCashOutSalesRequest, 
			@Value("${tcs.function.forex.body}") String tcsFunctionForex, 
			@Value("${tcs.function.getusercoreprivileges.filter.body}") String tcsFunctionGetUserCorePrivileges){
		
		this.TcsHost=TcsHost;
		this.TcsAuthFunctionName=TcsAuthFunctionName;
		this.TcsAllowedTerminalType=TcsAllowedTerminalType;
		this.TcsCallTimeout=TcsCallTimeout;
		
		this.TcsProxyUser=TcsProxyUser;
		this.TcsProxyPassword=TcsProxyPassword;
		
		this.TcsFunctionAuthenticate=TcsFunctionAuthenticate;
		this.TcsFunctionAccountExists = tcsFunctionAccountExists;
		this.TcsFunctionGetAccountInfoIsoByAccount = tcsFunctionGetAccountInfoIsoByAccount;
		this.TcsFunctionGetAccountInfoIsoByAlias = tcsFunctionGetAccountInfoIsoByAlias;
		this.TcsFunctionViewAccountType = tcsFunctionViewAccountType;
		this.TcsFunctionGetMyAccountInfo = tcsFunctionGetMyAccountInfo;
		this.TcsFunctionRequestTac = tcsFunctionRequestTac;
		this.TcsFunctionValidateTac = tcsFunctionValidateTac;
		this.TcsFunctionChangePassword = tcsFunctionChangePassword;
		this.TcsFunctionPayment = tcsFunctionPayment;
		this.TcsFunctionRedeem = tcsFunctionRedeem;
		this.TcsFunctionBillPay = tcsFunctionBillPay;
		this.TcsFunctionRefund = tcsFunctionRefund;
		this.TcsFunctionSalesRequest = tcsFunctionSalesRequest;
		this.TcsFunctionSalesRequestCheck = tcsFunctionSalesRequestCheck;
		this.TcsFunctionSalesRequestExec = tcsFunctionSalesRequestExec;
		this.TcsFunctionProxyTransaction = tcsFunctionProxyTransaction;
		this.TcsFunctionGetPrivileges = tcsFunctionGetPrivileges;
		this.TcsFunctionSignupUser = tcsFunctionSignupUser;
		this.TcsFunctionChangeUserStatus = tcsFunctionChangeUserStatus;
		this.TcsFunctionAddStaff = tcsFunctionAddStaff;
		this.TcsFunctionDeleteStaff = tcsFunctionDeleteStaff;
		this.TcsFunctionResetStaff = tcsFunctionResetStaff;
		this.TcsFunctionVerifyAccountLevel = tcsFunctionVerifyAccountLevel;
		this.TcsFunctionCreateAccountNoTac = tcsFunctionCreateAccountNoTac;
		this.TwoFactorAuthEnabled = twoFactorAuthEnabled;
		this.TcsFunctionBalanceMWallet = tcsFunctionBalanceMWallet;
		this.TcsFunctionBalanceProxyOwner = tcsFunctionBalanceProxyOwner;
		this.TcsFunctionChangeLanguage = tcsFunctionChangeLanguage;
		this.TcsFunctionSetAlias = tcsFunctionSetAlias;
		this.TcsFunctionSendVoucher = tcsFunctionSendVoucher;
		this.TcsFunctionSendToBank = tcsFunctionSendToBank;
		this.TcsFunctionRequestRefund = tcsFunctionRequestRefund;
		this.TcsFunctionRequestRefundApproval = tcsFunctionRequestRefundApproval;
		this.TcsFunctionResetMyUser = tcsFunctionResetMyUser;
		this.PgHost = pgHost;
		this.PgFunctionInquiry = pgFunctionInquiry;
		this.TCSFunctionFilteredCorePrivilegs = tcsFunctionFilteredCorePrivilegs;
		this.TCSFunctionRegisterSubscriber = tcsFunctionRegisterSubscriber;
		this.TCSFunctionUpdateSubscriberParam39 = tcsFunctionUpdateSubscriberParam39;
		this.TCSFunctionBCashOutSalesRequest = tcsFunctionBCashOutSalesRequest;
		this.TCSFunctionForex = tcsFunctionForex;
		this.TCSFunctionGetUserCorePrivileges = tcsFunctionGetUserCorePrivileges;
	}

	

	public String getTcsHost() {
		return TcsHost;
	}

	public String getTcsAuthFunctionName() {
		return TcsAuthFunctionName;
	}

	public String getTcsAllowedTerminalType() {
		return TcsAllowedTerminalType;
	}

	public Integer getTcsCallTimeout() {
		return TcsCallTimeout;
	}

	
	public String getTcsProxyUser() {
		return TcsProxyUser;
	}

	public String getTcsProxyPassword() {
		return TcsProxyPassword;
	}
	
	public String getTcsFunctionAuthenticate() {
		return TcsFunctionAuthenticate;
	}

	public String getTcsFunctionAccountExists() {
		return TcsFunctionAccountExists;
	}

	public String getTcsFunctionGetAccountInfoIsoByAccount() {
		return TcsFunctionGetAccountInfoIsoByAccount;
	}

	public String getTcsFunctionGetAccountInfoIsoByAlias() {
		return TcsFunctionGetAccountInfoIsoByAlias;
	}

	public String getTcsFunctionViewAccountType() {
		return TcsFunctionViewAccountType;
	}

	public String getTcsFunctionGetMyAccountInfo() {
		return TcsFunctionGetMyAccountInfo;
	}

	public String getTcsFunctionRequestTac() {
		return TcsFunctionRequestTac;
	}

	public String getTcsFunctionValidateTac() {
		return TcsFunctionValidateTac;
	}

	public String getTcsFunctionChangePassword() {
		return TcsFunctionChangePassword;
	}
	
	public String getTcsFunctionPayment() {
		return TcsFunctionPayment;
	}

	public String getTcsFunctionRedeem() {
		return TcsFunctionRedeem;
	}

	public String getTcsFunctionBillPay() {
		return TcsFunctionBillPay;
	}

	public String getTcsFunctionRefund() {
		return TcsFunctionRefund;
	}

	public String getTcsFunctionSalesRequest() {
		return TcsFunctionSalesRequest;
	}

	public String getTcsFunctionSalesRequestCheck() {
		return TcsFunctionSalesRequestCheck;
	}

	public String getTcsFunctionSalesRequestExec() {
		return TcsFunctionSalesRequestExec;
	}

	public String getTcsFunctionGetPrivileges() {
		return TcsFunctionGetPrivileges;
	}

	public String getTcsFunctionProxyTransaction() {
		return TcsFunctionProxyTransaction;
	}

	public String getTcsFunctionSignupUser() {
		return TcsFunctionSignupUser;
	}

	public String getTcsFunctionChangeUserStatus() {
		return TcsFunctionChangeUserStatus;
	}

	public String getTcsFunctionAddStaff() {
		return TcsFunctionAddStaff;
	}

	public String getTcsFunctionDeleteStaff() {
		return TcsFunctionDeleteStaff;
	}

	public String getTcsFunctionResetStaff() {
		return TcsFunctionResetStaff;
	}

	public String getTcsFunctionVerifyAccountLevel() {
		return TcsFunctionVerifyAccountLevel;
	}

	public String getTcsFunctionCreateAccountNoTac() {
		return TcsFunctionCreateAccountNoTac;
	}

	public String getTwoFactorAuthEnabled() {
		return TwoFactorAuthEnabled;
	}

	public String getTcsFunctionBalanceMWallet() {
		return TcsFunctionBalanceMWallet;
	}

	public String getTcsFunctionBalanceProxyOwner() {
		return TcsFunctionBalanceProxyOwner;
	}

	public String getTcsFunctionChangeLanguage() {
		return TcsFunctionChangeLanguage;
	}
	
	public String getTcsFunctionSetAlias() {
		return TcsFunctionSetAlias;
	}

	public String getTcsFunctionSendVoucher() {
		return TcsFunctionSendVoucher;
	}

	public String getTcsFunctionSendToBank() {
		return TcsFunctionSendToBank;
	}

	public String getTcsFunctionRequestRefund() {
		return TcsFunctionRequestRefund;
	}

	public String getTcsFunctionRequestRefundApproval() {
		return TcsFunctionRequestRefundApproval;
	}

	public String getTcsFunctionResetMyUser() {
		return TcsFunctionResetMyUser;
	}

	public String getPgHost() {
		return PgHost;
	}

	public String getPgFunctionInquiry() {
		return PgFunctionInquiry;
	}

	public String getTCSFunctionFilteredCorePrivilegs() {
		return TCSFunctionFilteredCorePrivilegs;
	}

	public String getTCSFunctionRegisterSubscriber() {
		return TCSFunctionRegisterSubscriber;
	}

	public String getTCSFunctionUpdateSubscriberParam39() {
		return TCSFunctionUpdateSubscriberParam39;
	}

	public String getTCSFunctionBCashOutSalesRequest() {
		return TCSFunctionBCashOutSalesRequest;
	}

	public String getTCSFunctionForex() {
		return TCSFunctionForex;
	}



	public String getTCSFunctionGetUserCorePrivileges() {
		return TCSFunctionGetUserCorePrivileges;
	}
}
