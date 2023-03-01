package com.onecashye.web.security.helper;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MappingParamsHelper {

	
	public static Map<String,String> mapAccountInfo(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Customer_ID",m.get("param1"));
		result.put("First_Name",m.get("param3"));
		result.put("Family_Name",m.get("param4"));
		result.put("Second_Name",m.get("param19"));
		result.put("Third_Name",m.get("param47"));
		result.put("Full_Name",m.get("param22"));
		result.put("Address",m.get("param27"));
		result.put("National_ID",m.get("param6"));
		result.put("ID_Type",m.get("param7"));
		result.put("Country",m.get("param37"));
		result.put("Province",m.get("param14"));
		result.put("City",m.get("param13"));
		result.put("DOB",m.get("param10"));
		result.put("Occupation",m.get("param11"));
		result.put("Gender",m.get("param18"));
		result.put("ID_Issuance_Date",m.get("param142"));
		result.put("ID_Expiry_Date",m.get("param20"));
		result.put("Email",m.get("param26"));
		result.put("Account_Name",m.get("param178"));
		result.put("Docs_On_File",m.get("param39"));//Added 02082022
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapViewAccountType(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		try {
			List<Map<String,Object>> lookup = getGroupNames();
			
			for(Map<String,Object> map : lookup) {
				
				Integer x = Integer.decode(String.format("%s", m.get("param6")));
				Integer y = Integer.decode(String.format("%s", map.get("Group_Id")));
				if(x==y) {
					
					result.put("Account_Group_Name",String.format("%s", map.get("Group_Name")));
					result.put("Account_Layer_Name",String.format("%s", map.get("Layer_Name")));
				}
					
					
			}
		} catch (JsonMappingException e) {
			
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
	
			e.printStackTrace();
		}
		
		result.put("Account_MSISDN",m.get("param1"));
		result.put("Account_ID",m.get("param2"));
		result.put("Account_Name",m.get("param3"));
		result.put("Account_Customer_Type",m.get("param4"));
		result.put("Account_Layer_ID",m.get("param5"));
		result.put("Account_Group_ID",m.get("param6"));
		result.put("Account_Status",m.get("param7"));
		
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	
	@SuppressWarnings("unchecked")
	private static List<Map<String,Object>> getGroupNames() throws IOException {
		
		//Added 6 new groups on 05042022
		
		
		String resName="TelepinGroups";
		
		File resource = new ClassPathResource(resName).getFile();
		System.out.println(resource.getAbsolutePath());
		
		byte[] fileBytes= Files.readAllBytes(resource.toPath());
		String s = new String(fileBytes, StandardCharsets.UTF_8);
		System.out.println(s);
	
		
		Map<String, Object> result;
		
		result = new ObjectMapper().readValue(s, HashMap.class);
		
		return (List<Map<String, Object>>) result.get("Groups");
	}

	/*
	 * PAYMENT API REGION
	 */
	
	public static Map<String,String> mapBusinessCashOutPaymentCheck(Map<String,String> m){
		
		/*
		 * 21082022
		 * To check payment to get the details for Business CashOut Maker as SALESREQUEST API in check=true mode doesn't get details
		 */
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Destination_Balance",m.get("param1"));
		result.put("Target_MSISDN",m.get("param2"));
		result.put("Payment_Amount",m.get("param6"));
		result.put("Total_Fees",m.get("param9"));
		result.put("Target_Account",m.get("param11"));
		result.put("Target_Fullname",m.get("param14"));
		
		//result.put("Extra_Input",m.get("param31"));
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}

	public static Map<String,String> mapCashPaymentCheck(Map<String,String> m){
		
		//For Customer Cash In, Business Cash In & Business Cash Out
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Destination_Balance",m.get("param1"));
		result.put("Target_MSISDN",m.get("param2"));
		result.put("Payment_Amount",m.get("param6"));
		result.put("Total_Fees",m.get("param9"));
		result.put("Target_Account",m.get("param40"));//Changed on 04102022 it was param11
		result.put("Target_Fullname",m.get("param14"));
		
		//result.put("Extra_Input",m.get("param31"));
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapCashPayment(Map<String,String> m){
		
		//For Customer Cash In, Business Cash In & Business Cash Out
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Transaction_ID",m.get("param1"));
		result.put("Sales_Order",m.get("param11"));//Added 07062022
		result.put("Original_Amount",m.get("param2"));
		result.put("Payment_Amount",m.get("param3"));
		result.put("Agent_Balance",m.get("param14"));
		result.put("Agent_Commission",m.get("param20").replace("-", ""));
		result.put("Target_Account",m.get("param86"));
		
		//Added 24052022
		result.put("Total_Amount",m.get("param18"));
		
		//result.put("Extra_Input",m.get("param31"));
		
		//Added 03-03-2022
		result.put("Source_Fees1",m.get("param19"));
		result.put("Source_Fees2",m.get("param20"));
		result.put("Source_Fees3",m.get("param21"));
		result.put("Source_Fees4",m.get("param22"));
		
		result.put("Destination_Fees1",m.get("param23"));
		result.put("Destination_Fees2",m.get("param24"));
		result.put("Destination_Fees3",m.get("param25"));
		result.put("Destination_Fees4",m.get("param26"));
		/////////////////////////////////////////////////
		
		//Added 21-11-2022 For date time
		result.put("Created_On",m.get("param83"));
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapSellFloatCheck(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Destination_Balance",m.get("param1"));
		result.put("Target_MSISDN",m.get("param2"));
		result.put("Payment_Amount",m.get("param6"));
		result.put("Total_Fees",m.get("param9"));
		result.put("Target_Account",m.get("param11"));
		result.put("Target_Fullname",m.get("param14"));
		
		//result.put("Extra_Input",m.get("param31"));
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	public static Map<String,String> mapDeleteMoneyCheck(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Destination_Balance",m.get("param1"));
		result.put("Target_MSISDN",m.get("param2"));
		result.put("Payment_Amount",m.get("param6"));
		result.put("Total_Fees",m.get("param9"));
		result.put("Target_Account",m.get("param11"));
		result.put("Target_Fullname",m.get("param14"));
		
		//result.put("Extra_Input",m.get("param31"));
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	public static Map<String,String> mapSellFloat(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Transaction_ID",m.get("param1"));
		result.put("Sales_Order",m.get("param11"));//Added 07062022
		result.put("Original_Amount",m.get("param2"));
		result.put("Payment_Amount",m.get("param3"));
		result.put("Agent_Balance",m.get("param14"));
		result.put("Agent_Commission",m.get("param20").replace("-", ""));
		result.put("Target_Account",m.get("param86"));
		
		//Added 24052022
		result.put("Total_Amount",m.get("param18"));
				
		//result.put("Extra_Input",m.get("param31"));
		
		//Added 03-03-2022
		result.put("Source_Fees1",m.get("param19"));
		result.put("Source_Fees2",m.get("param20"));
		result.put("Source_Fees3",m.get("param21"));
		result.put("Source_Fees4",m.get("param22"));
		
		result.put("Destination_Fees1",m.get("param23"));
		result.put("Destination_Fees2",m.get("param24"));
		result.put("Destination_Fees3",m.get("param25"));
		result.put("Destination_Fees4",m.get("param26"));
		/////////////////////////////////////////////////
		
		//Added 21-11-2022 For date time
				result.put("Created_On",m.get("param83"));
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	public static Map<String,String> mapDeleteMoney(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Transaction_ID",m.get("param1"));
		result.put("Sales_Order",m.get("param11"));//Added 07062022
		result.put("Original_Amount",m.get("param2"));
		result.put("Payment_Amount",m.get("param3"));
		result.put("Agent_Balance",m.get("param14"));
		result.put("Agent_Commission",m.get("param20").replace("-", ""));
		result.put("Target_Account",m.get("param86"));
		
		//Added 24052022
		result.put("Total_Amount",m.get("param18"));
				
		//result.put("Extra_Input",m.get("param31"));
		
		//Added 03-03-2022
		result.put("Source_Fees1",m.get("param19"));
		result.put("Source_Fees2",m.get("param20"));
		result.put("Source_Fees3",m.get("param21"));
		result.put("Source_Fees4",m.get("param22"));
		
		result.put("Destination_Fees1",m.get("param23"));
		result.put("Destination_Fees2",m.get("param24"));
		result.put("Destination_Fees3",m.get("param25"));
		result.put("Destination_Fees4",m.get("param26"));
		/////////////////////////////////////////////////
		
		//Added 21-11-2022 For date time
		result.put("Created_On",m.get("param83"));
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapPushFloatCheck(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Destination_Balance",m.get("param1"));
		result.put("Target_MSISDN",m.get("param2"));
		result.put("Payment_Amount",m.get("param6"));
		result.put("Total_Fees",m.get("param9"));
		result.put("Target_Account",m.get("param11"));
		result.put("Target_Fullname",m.get("param14"));
		
		//result.put("Extra_Input",m.get("param31"));
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapPushFloat(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Transaction_ID",m.get("param1"));
		result.put("Sales_Order",m.get("param11"));//Added 07062022
		result.put("Original_Amount",m.get("param2"));
		result.put("Payment_Amount",m.get("param3"));
		result.put("Agent_Balance",m.get("param14"));
		result.put("Agent_Commission",m.get("param20").replace("-", ""));
		result.put("Target_Account",m.get("param86"));
		
		//Added 24052022
		result.put("Total_Amount",m.get("param18"));
				
		//result.put("Extra_Input",m.get("param31"));
		
		result.put("Source_Fees1",m.get("param19"));
		result.put("Source_Fees2",m.get("param20"));
		result.put("Source_Fees3",m.get("param21"));
		result.put("Source_Fees4",m.get("param22"));
		
		result.put("Destination_Fees1",m.get("param23"));
		result.put("Destination_Fees2",m.get("param24"));
		result.put("Destination_Fees3",m.get("param25"));
		result.put("Destination_Fees4",m.get("param26"));
		
		//Added 21-11-2022 For date time
		result.put("Created_On",m.get("param83"));
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapPullFloatCheck(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Destination_Balance",m.get("param1"));
		result.put("Target_MSISDN",m.get("param2"));
		result.put("Payment_Amount",m.get("param6"));
		result.put("Total_Fees",m.get("param9"));
		result.put("Target_Account",m.get("param11"));
		result.put("Target_Fullname",m.get("param14"));
		
		//result.put("Extra_Input",m.get("param31"));
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapPullFloat(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Transaction_ID",m.get("param1"));
		result.put("Sales_Order",m.get("param11"));//Added 07062022
		result.put("Original_Amount",m.get("param2"));
		result.put("Payment_Amount",m.get("param3"));
		result.put("Agent_Balance",m.get("param14"));
		result.put("Agent_Commission",m.get("param20").replace("-", ""));
		result.put("Target_Account",m.get("param86"));
		
		//Added 24052022
		result.put("Total_Amount",m.get("param18"));
				
		//result.put("Extra_Input",m.get("param31"));
		
		result.put("Source_Fees1",m.get("param19"));
		result.put("Source_Fees2",m.get("param20"));
		result.put("Source_Fees3",m.get("param21"));
		result.put("Source_Fees4",m.get("param22"));
		
		result.put("Destination_Fees1",m.get("param23"));
		result.put("Destination_Fees2",m.get("param24"));
		result.put("Destination_Fees3",m.get("param25"));
		result.put("Destination_Fees4",m.get("param26"));
		
		//Added 21-11-2022 For date time
		result.put("Created_On",m.get("param83"));
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
		
	public static Map<String,String> mapRedeemCheckPayment(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Destination_Balance",m.get("param1"));
		result.put("Target_MSISDN",m.get("param2"));
		result.put("Payment_Amount",m.get("param6"));
		result.put("Total_Fees",m.get("param9"));
		result.put("Target_Account","");
		result.put("Target_Fullname","");
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapRedeemPayment(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Transaction_ID",m.get("param1"));
		result.put("Sales_Order",m.get("param11"));//Added 07062022
		result.put("Original_Amount",m.get("param2"));
		result.put("Payment_Amount",m.get("param3"));
		result.put("Agent_Balance",m.get("param14"));
		result.put("Agent_Commission",m.get("param24"));
		result.put("Voucher_Number",m.get("param31"));
		
		//Added 02062022
		result.put("Total_Amount",m.get("param18"));
		
		result.put("Source_Fees1",m.get("param19"));
		result.put("Source_Fees2",m.get("param20"));
		result.put("Source_Fees3",m.get("param21"));
		result.put("Source_Fees4",m.get("param22"));
		
		result.put("Destination_Fees1",m.get("param23"));
		result.put("Destination_Fees2",m.get("param24"));
		result.put("Destination_Fees3",m.get("param25"));
		result.put("Destination_Fees4",m.get("param26"));
		
		//Added 21-11-2022 For date time
		result.put("Created_On",m.get("param83"));
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapCheckVoucherPayment(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Destination_Balance",m.get("param1"));
		result.put("Target_MSISDN",m.get("param2"));
		result.put("Payment_Amount",m.get("param6"));
		result.put("Total_Fees",m.get("param9"));
		result.put("Target_Account","");
		result.put("Target_Fullname","");
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapVoucherPayment(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Transaction_ID",m.get("param1"));
		result.put("Sales_Order",m.get("param11"));//Added 07062022
		result.put("Original_Amount",m.get("param2"));
		result.put("Payment_Amount",m.get("param3"));
		result.put("Voucher_Number",m.get("param6"));
		result.put("Agent_Balance",m.get("param14"));
		result.put("Agent_Commission",m.get("param24"));
		
		//Added 02062022
		result.put("Total_Amount",m.get("param18"));
				
		result.put("Source_Fees1",m.get("param19"));
		result.put("Source_Fees2",m.get("param20"));
		result.put("Source_Fees3",m.get("param21"));
		result.put("Source_Fees4",m.get("param22"));
		
		result.put("Destination_Fees1",m.get("param23"));
		result.put("Destination_Fees2",m.get("param24"));
		result.put("Destination_Fees3",m.get("param25"));
		result.put("Destination_Fees4",m.get("param26"));
		
		//Added 21-11-2022 For date time
		result.put("Created_On",m.get("param83"));
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapBillCheckPayment(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Destination_Balance",m.get("param1"));
		result.put("Target_MSISDN",m.get("param2"));
		result.put("Payment_Amount",m.get("param6"));
		result.put("Total_Fees",m.get("param9"));
		result.put("Target_Account","");
		result.put("Target_Fullname","");
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));

		return result;
	}
	
	public static Map<String,String> mapBillPayment(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Transaction_ID",m.get("param1"));
		result.put("Sales_Order",m.get("param11"));//Added 07062022
		result.put("Original_Amount",m.get("param2"));
		result.put("Payment_Amount",m.get("param3"));
		result.put("Agent_Balance",m.get("param14"));
		result.put("Total_Fees",m.get("param19"));
		result.put("Extra_Info",m.get("param31"));
		
		//Added 02062022
		result.put("Total_Amount",m.get("param18"));
		
		//Added 21-11-2022 For date time
		result.put("Created_On",m.get("param83"));
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapCheckBankPayment(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Destination_Balance",m.get("param1"));
		result.put("Target_MSISDN",m.get("param2"));
		result.put("Payment_Amount",m.get("param6"));
		result.put("Total_Fees",m.get("param9"));
		result.put("Target_Account",m.get("param27"));
		result.put("Target_Fullname",m.get("param19"));
		
		//result.put("Extra_Input",m.get("param31"));
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));

		return result;
	}
	
	public static Map<String,String> mapBankPayment(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Transaction_ID",m.get("param1"));
		result.put("Sales_Order",m.get("param11"));//Added 07062022
		result.put("Original_Amount",m.get("param2"));
		result.put("Payment_Amount",m.get("param3"));
		result.put("Total_Amount",m.get("param18"));
		result.put("Target_Account",m.get("param73"));
		
		//result.put("Extra_Input",m.get("param31"));
		//Added 02062022
		result.put("Total_Amount",m.get("param18"));
		
		result.put("Source_Fees1",m.get("param19"));
		result.put("Source_Fees2",m.get("param20"));
		result.put("Source_Fees3",m.get("param21"));
		result.put("Source_Fees4",m.get("param22"));
		
		result.put("Destination_Fees1",m.get("param23"));
		result.put("Destination_Fees2",m.get("param24"));
		result.put("Destination_Fees3",m.get("param25"));
		result.put("Destination_Fees4",m.get("param26"));
		
		//Added 21-11-2022 For date time
		result.put("Created_On",m.get("param83"));
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapB2AllCheckPayment(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Destination_Balance",m.get("param1"));
		result.put("Target_MSISDN",m.get("param2"));
		result.put("Payment_Amount",m.get("param6"));
		result.put("Total_Fees",m.get("param9"));
		result.put("Target_Account",m.get("param11"));
		result.put("Target_Fullname",m.get("param14"));
		
		//result.put("Extra_Input",m.get("param31"));
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapB2AllPayment(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Transaction_ID",m.get("param1"));
		result.put("Sales_Order",m.get("param11"));//Added 07062022
		result.put("Original_Amount",m.get("param2"));
		result.put("Payment_Amount",m.get("param3"));
		result.put("Agent_Balance",m.get("param14"));
		result.put("Total_Fees",m.get("param20"));
		result.put("Target_Account",m.get("param86"));
		
		//Added 24052022
		result.put("Total_Amount",m.get("param18"));
				
		//result.put("Extra_Input",m.get("param31"));
		
		result.put("Source_Fees1",m.get("param19"));
		result.put("Source_Fees2",m.get("param20"));
		result.put("Source_Fees3",m.get("param21"));
		result.put("Source_Fees4",m.get("param22"));
		
		result.put("Destination_Fees1",m.get("param23"));
		result.put("Destination_Fees2",m.get("param24"));
		result.put("Destination_Fees3",m.get("param25"));
		result.put("Destination_Fees4",m.get("param26"));
		
		//Added 21-11-2022 For date time
		result.put("Created_On",m.get("param83"));
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapRefund(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		result.put("Reference_Number",m.get("param1"));
		result.put("Sales_Order",m.get("param2"));
		
		return result;
	}

	public static Map<String,String> mapCheckSalesRequest(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapSalesRequest(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Request_ID",m.get("param1"));
		result.put("Original_Amount",m.get("param2"));
		result.put("Payment_Amount",m.get("param3"));
		result.put("Source_Fees1",m.get("param4"));
		result.put("Source_Fees2",m.get("param5"));
		result.put("Source_Fees3",m.get("param6"));
		result.put("Destination_Fees1",m.get("param8"));
		result.put("Destination_Fees2",m.get("param9"));
		result.put("Destination_Fees3",m.get("param10"));
		result.put("Payable_Amount",m.get("param15"));
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapCheckRefundRequest(Map<String,String> m){
		
		/*
		 * Added 02-04-2022
		 */
		
		Map<String,String> result = new HashMap<>();
		
		
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapRefundRequest(Map<String,String> m){
		
		/*
		 * Added 02-04-2022
		 */
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Request_ID",m.get("param1"));
		result.put("Original_Amount",m.get("param2"));
		result.put("Payment_Amount",m.get("param3"));
		result.put("Source_Fees1",m.get("param4"));
		result.put("Source_Fees2",m.get("param5"));
		result.put("Source_Fees3",m.get("param6"));
		result.put("Destination_Fees1",m.get("param8"));
		result.put("Destination_Fees2",m.get("param9"));
		result.put("Destination_Fees3",m.get("param10"));
		result.put("Payable_Amount",m.get("param15"));
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapApproveRefundRequest(Map<String,String> m){
		
		/*
		 * Added 02-04-2022
		 */
		
		Map<String,String> result = new HashMap<>();
		
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapSalesRequestCheck(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		switch(m.get("param1")) {
		
			case "RQS":
				result.put("Request_Status","Pending Request:بانتظار الموافقة");
				break;
			case "HLD":
				result.put("Request_Status","Executing:قيد التنفيذ");
				break;
			case "PST":
				result.put("Request_Status","Successfully Transferred:تمت العملية بنجاح");
				break;
			case "DCL":
				result.put("Request_Status","Request Declined:تم رفض العملية");
				break;
			default:
				result.put("Request_Status","Unknown:غير معرف");
			
		}
		
		result.put("TCS_Status",m.get("param1"));
		result.put("Reference",m.get("param2"));
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapCheckSalesRequestExec(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Destination_Balance",m.get("param1"));
		result.put("Target_MSISDN",m.get("param2"));
		result.put("Payment_Amount",m.get("param6"));
		result.put("Total_Fees",m.get("param9"));
		result.put("Target_Account",m.get("param11"));
		result.put("Target_Fullname",m.get("param14"));
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}

	public static Map<String,String> mapSalesRequestExec(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Transaction_ID",m.get("param1"));
		result.put("Sales_Order",m.get("param11"));//Added 07062022
		result.put("Original_Amount",m.get("param2"));
		result.put("Payment_Amount",m.get("param3"));
		result.put("Agent_Balance",m.get("param14"));
		result.put("Total_Fees",m.get("param20"));
		result.put("Target_Account",m.get("param86"));
		
		//Added 24052022
		result.put("Total_Amount",m.get("param18"));
		
		result.put("Source_Fees1",m.get("param19"));
		result.put("Source_Fees2",m.get("param20"));
		result.put("Source_Fees3",m.get("param21"));
		result.put("Source_Fees4",m.get("param22"));
		
		result.put("Destination_Fees1",m.get("param23"));
		result.put("Destination_Fees2",m.get("param24"));
		result.put("Destination_Fees3",m.get("param25"));
		result.put("Destination_Fees4",m.get("param26"));
		
		//Added 21-11-2022 For date time
		result.put("Created_On",m.get("param83"));
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapDummySalesRequestExec(){
		
		/*
		 * 06062022
		 * 
		 * Added for multi checker flow
		 */
		Map<String,String> result = new HashMap<>();
		
		result.put("Transaction_ID","");
		result.put("Original_Amount","0.0");
		result.put("Payment_Amount","0.0");
		result.put("Agent_Balance","0.0");
		result.put("Total_Fees","0.0");
		result.put("Target_Account","");
		
		
		result.put("Total_Amount","0.0");
		
		result.put("Source_Fees1","0.0");
		result.put("Source_Fees2","0.0");
		result.put("Source_Fees3","0.0");
		result.put("Source_Fees4","0.0");
		
		result.put("Destination_Fees1","0.0");
		result.put("Destination_Fees2","0.0");
		result.put("Destination_Fees3","0.0");
		result.put("Destination_Fees4","0.0");
		
		result.put("Result","0");
		result.put("Message","APPROVED");
		
		return result;
	}
	
	public static Map<String,String> mapDummySalesRequestExec(Map<String,String> salesRequest){
		
		/*
		 * 18072022
		 * 
		 * Added for multi checker flow to return sales request details instead of empty details 
		 */
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Transaction_ID","");
		result.put("Request_ID",salesRequest.get("Request_Id"));
		result.put("Original_Amount",salesRequest.get("Original_Amount"));
		result.put("Payment_Amount",salesRequest.get("Payment_Amount"));
		result.put("Agent_Balance","0.0");
		result.put("Total_Fees","0.0");
		result.put("Target_Account",salesRequest.get("Destination_Account"));
		
		
		result.put("Total_Amount",salesRequest.get("Payment_Amount"));
		
		result.put("Source_Fees1",salesRequest.get("Source_Fees1"));
		result.put("Source_Fees2",salesRequest.get("Source_Fees2"));
		result.put("Source_Fees3",salesRequest.get("Source_Fees3"));
		result.put("Source_Fees4","0.0");
		
		result.put("Destination_Fees1",salesRequest.get("Destination_Fees1"));
		result.put("Destination_Fees2",salesRequest.get("Destination_Fees2"));
		result.put("Destination_Fees3",salesRequest.get("Destination_Fees3"));
		result.put("Destination_Fees4","0.0");
		
		result.put("Result","0");
		result.put("Message","APPROVED");
		
		return result;
	}
	
	public static Map<String,String> mapDummyCashOutSalesRequestApproval(Map<String,Object> salesRequest,String action){
		
		/*
		 * 22082022
		 * 
		 * Added for multi checker flow to return sales request details instead of empty details for CashOut when Checker1 approves only
		 */
		
		
		Map<String,String> newMap =new HashMap<String,String>();
		for (Map.Entry<String, Object> entry : salesRequest.entrySet()) {
		       if(entry.getValue() instanceof String){
		            newMap.put(entry.getKey(), (String) entry.getValue());
		          }
		 }
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Transaction_ID",newMap.get("LOCAL_REQUEST_ID"));
		result.put("Original_Amount",newMap.get("ORIGINAL_AMOUNT"));
		result.put("Payment_Amount",newMap.get("PAYABLE_AMOUNT"));
		result.put("Agent_Balance","0.0");
		result.put("Total_Fees","0.0");
		result.put("Target_Account",newMap.get("DEST_MSISDN"));
		
		
		result.put("Total_Amount",newMap.get("PAYABLE_AMOUNT"));
		result.put("Currency",newMap.get("CURRENCY"));
		
		result.put("Source_Fees1",newMap.get("SOURCE_FEES1"));
		result.put("Source_Fees2","0.0");
		result.put("Source_Fees3","0.0");
		result.put("Source_Fees4","0.0");
		
		result.put("Destination_Fees1","0.0");
		result.put("Destination_Fees2","0.0");
		result.put("Destination_Fees3","0.0");
		result.put("Destination_Fees4","0.0");
		
		result.put("Result","0");
		result.put("Message",action);
		
		return result;
	}
	
	public static Map<String,String> mapDummyBillPayment(){
		
		/*
		 * 03072022
		 * 
		 * Added for dev mode bill payment
		 */
		Map<String,String> result = new HashMap<>();
		
		result.put("Transaction_ID","1");
		result.put("Original_Amount","0.0");
		result.put("Payment_Amount","0.0");
		result.put("Agent_Balance","0.0");
		result.put("Total_Fees","0.0");
		result.put("Target_Account","");
		
		
		result.put("Total_Amount","0.0");
		
		result.put("Source_Fees1","0.0");
		result.put("Source_Fees2","0.0");
		result.put("Source_Fees3","0.0");
		result.put("Source_Fees4","0.0");
		
		result.put("Destination_Fees1","0.0");
		result.put("Destination_Fees2","0.0");
		result.put("Destination_Fees3","0.0");
		result.put("Destination_Fees4","0.0");
		
		result.put("Result","0");
		result.put("Message","Bill payment is done successfully DEV MODE");
		
		return result;
	}
	
	public static Map<String,String> mapDummyCheckBillPayment(){
		
		/*
		 * 05092022
		 * 
		 * Added for dev mode check bill payment
		 */
		Map<String,String> result = new HashMap<>();
		
		result.put("Transaction_ID","1");
		result.put("Original_Amount","0.0");
		result.put("Payment_Amount","0.0");
		result.put("Agent_Balance","0.0");
		result.put("Total_Fees","0.0");
		result.put("Target_Account","");
		
		
		result.put("Total_Amount","0.0");
		
		result.put("Source_Fees1","0.0");
		result.put("Source_Fees2","0.0");
		result.put("Source_Fees3","0.0");
		result.put("Source_Fees4","0.0");
		
		result.put("Destination_Fees1","0.0");
		result.put("Destination_Fees2","0.0");
		result.put("Destination_Fees3","0.0");
		result.put("Destination_Fees4","0.0");
		
		result.put("Result","0");
		result.put("Message","Bill payment is done successfully DEV MODE");
		
		return result;
	}
	
	/*
	 * ACCOUNT MANAGEMENT API REGION
	 */
	
	public static Map<String,String> mapCreateAccountNoTacResponse(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Result",m.get("Result"));
		result.put("Account_ID",m.get("param1"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapSimpleResponse(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapBalanceWalletResponse(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Wallet1",m.get("param1"));
		result.put("Wallet2",m.get("param2"));
		result.put("Wallet3",m.get("param3"));
		result.put("Wallet4",m.get("param14"));
		
		
		result.put("Result",m.get("Result"));
		result.put("Message",m.get("Message"));
		
		return result;
	}
	
	public static Map<String,String> mapBalanceProxyOwnerResponse(Map<String,String> m){
		
		Map<String,String> result = new HashMap<>();
		
		result.put("Wallet1",m.get("param1"));
		result.put("Wallet2",m.get("param2"));
		result.put("Wallet3",m.get("param3"));
		result.put("Wallet4",m.get("param14"));
		
		
		return result;
	}
	

}
