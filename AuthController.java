package jp.co.internous.angular.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import jp.co.internous.angular.model.domain.MstUser;
import jp.co.internous.angular.model.form.UserForm;
import jp.co.internous.angular.model.mapper.MstUserMapper;
import jp.co.internous.angular.model.mapper.TblCartMapper;
import jp.co.internous.angular.model.session.LoginSession;

@RestController
@RequestMapping("/angular/auth")
public class AuthController {

	@Autowired
	LoginSession loginSession;
	
	@Autowired
	MstUserMapper mstUserMapper;

	@Autowired
	TblCartMapper tblCartMapper;
	
	
	@PostMapping("/login")
	public String login(@RequestBody UserForm form, Model model) {
		
		MstUser user = mstUserMapper.findByUserNameAndPassword(form.getUserName(), form.getPassword());

		if(user != null) {
			
			tblCartMapper.updateUserId(user.getId(),loginSession.getTmpUserId());
			
			loginSession.setUserId(user.getId());
			loginSession.setTmpUserId(0);
			loginSession.setUserName(user.getUserName());
			loginSession.setPassword(user.getPassword());
			loginSession.setLogined(true);
		
			model.addAttribute("loginSession", loginSession);
			
			Gson gson = new Gson();
			
			return gson.toJson(user);
			
		} else {
			return "1";
		}
	}
	
	
	@GetMapping("/logout")
	public String logout(Model model) {
		
		loginSession.setUserId(0);
		loginSession.setTmpUserId(0);
		loginSession.setUserName(null);
		loginSession.setPassword(null);
		loginSession.setLogined(false);
		
		model.addAttribute("loginSession", loginSession);	
		
		return "0";
	}
	
	
	@RequestMapping("/resetPassword")
	public String resetPassword(@RequestBody UserForm f) {
		String message = "パスワードが再設定されました。";
		String newPassword = f.getNewPassword();
		String newPasswordConfirm = f.getNewPasswordConfirm();
		
		MstUser user = mstUserMapper.findByUserNameAndPassword(f.getUserName(), f.getPassword());

		if (user == null) {
			return "現在のパスワードが正しくありません。";
		}
		
		if (user.getPassword().equals(newPassword)) {
			return "現在のパスワードと同一文字列が入力されました。";
		}
		
		if (!newPassword.equals(newPasswordConfirm)) {
			return "新パスワードと確認用パスワードが一致しません。";
		}
		// mst_userとloginSessionのパスワードを更新する
		mstUserMapper.updatePassword(user.getUserName(), f.getNewPassword());
		loginSession.setPassword(f.getNewPassword());
		
		
		return message;
	}

	
}
