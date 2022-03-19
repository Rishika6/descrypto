package deshaw.dae.descrypto.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import deshaw.dae.descrypto.domain.User;
import deshaw.dae.descrypto.domain.Wallet;
import deshaw.dae.descrypto.domain.demo;
import deshaw.dae.descrypto.services.DashboardService;
import deshaw.dae.descrypto.services.UserService;
import deshaw.dae.descrypto.services.WalletService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

import java.util.List;

@RestController
@RequestMapping("/user")
public class fundsController {
    @Autowired
    private WalletService walletservice;
    @Autowired
    private UserService userservice;

    @RequestMapping(value = "/addFund", method= RequestMethod.PUT)
    public ResponseEntity<?> addFund(@RequestBody ObjectNode objectnode) {
        String assetName = objectnode.get("assetName").asText();
       int amountToBeAdded = objectnode.get("amountToBeAdded").asInt();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName();
        User user = userservice.findByUserName(userName);
        Wallet wallet = walletservice.findWallet(user.getUserId(), assetName);
        if(wallet == null) {
            //System.out.print("inside null wallet");
            walletservice.addNewWallet(user.getUserId(), assetName, amountToBeAdded);
        }
        else {
            walletservice.addFund(user.getUserId(), assetName, amountToBeAdded);
        }
        JSONObject obj = new JSONObject();
        String message = Integer.toString(amountToBeAdded)+" coins for " + assetName + " has been added successfully in " + userName + "'s spot wallet!";
        obj.put("success_message", message);
        return new ResponseEntity<>(obj,HttpStatus.OK);
    }


    @RequestMapping(value = "/withdrawFunds", method= RequestMethod.PUT)
    public ResponseEntity<?> withDrawFunds(@RequestBody ObjectNode objectnode) {
        JSONObject obj = new JSONObject();
        String assetName = objectnode.get("assetName").asText();
        int amountToBeDeducted = objectnode.get("amountToBeDeducted").asInt();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName();
        User user = userservice.findByUserName(userName);
        Wallet wallet = walletservice.findWallet(user.getUserId(), assetName);
        if(wallet == null) {

            String message = Integer.toString(amountToBeDeducted) + " cannot be deducted as no such " + assetName + "asset exists in the spot wallet of " + userName;
            obj.put("failure_message", message);
            return new ResponseEntity<>(obj, HttpStatus.BAD_REQUEST);
        }
        else {
            int assetAvailableCoins = walletservice.getAssetCoins(user.getUserId(), assetName);
            if (assetAvailableCoins < amountToBeDeducted) {

                String message = Integer.toString(amountToBeDeducted) + " coins cannot be deducted as total number of coins for " + assetName + " is: " + Integer.toString(assetAvailableCoins) + " which is less than the coins to be deducted";
                obj.put("failure_message", message);
                return new ResponseEntity<>(obj,HttpStatus.BAD_REQUEST);
            } else {
                walletservice.withdrawFund(user.getUserId(), assetName, amountToBeDeducted);

                String message = Integer.toString(amountToBeDeducted) + " has been deducted from the spot wallet of " + userName + " for asset : " + assetName;
                obj.put("failure_message", message);
                return new ResponseEntity<>(obj,HttpStatus.OK);
            }
        }


    }


}