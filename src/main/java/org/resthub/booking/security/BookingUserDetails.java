package org.resthub.booking.security;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Object used to store informations about logged user. Internally used by
 * spring-security
 */
public class BookingUserDetails implements UserDetails {

	private static final long serialVersionUID = -492528595540877572L;

	private List<GrantedAuthority> authorities;

	private String password;
	private String username;

	public BookingUserDetails(String username) {
		this.username = username;
		this.authorities = new ArrayList<GrantedAuthority>();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<GrantedAuthority> getAuthorities() {
		return Collections.unmodifiableList(this.authorities);
	}

	public void addAuthority(GrantedAuthority authority) {
		this.authorities.add(authority);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAccountNonExpired() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAccountNonLocked() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCredentialsNonExpired() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEnabled() {
		return true;
	}
}
