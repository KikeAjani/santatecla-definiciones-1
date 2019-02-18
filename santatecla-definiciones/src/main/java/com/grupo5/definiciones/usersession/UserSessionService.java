package com.grupo5.definiciones.usersession;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.grupo5.definiciones.usersession.Tab;
import com.grupo5.definiciones.usersession.UserSessionInfoComponent;

@Service
public class UserSessionService {
	
	@Autowired
	private UserSessionInfoComponent userSession;
	
	/*public void addUserToModel(Model model) {
		boolean logged = userSession.getLoggedUser() != null;
		model.addAttribute("logged", logged);
		if(logged) {
			model.addAttribute("admin", userSession.getLoggedUser().getRoles().contains("ROLE_ADMIN"));
			model.addAttribute("userName", userSession.getLoggedUser().getName());
		}
	}*/

	public void setActive(String tabName) {
		userSession.setActive(tabName);
	}

	public List<Tab> getOpenTabs() {
		return userSession.getOpenTabs();
	}

	public void addTab(Tab tab) {
		userSession.addTab(tab);
	}
}