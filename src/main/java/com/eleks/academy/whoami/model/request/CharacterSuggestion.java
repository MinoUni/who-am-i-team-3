package com.eleks.academy.whoami.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharacterSuggestion {

//	@NotBlank(message = "Nickname may not be blank")
//	@Size(min = 2, max = 50, message = "Nickname must be between 2 and 50 characters long")
//	private String nickname;

	@NotBlank(message = "Character may not be blank")
	@Size(min = 2, max = 50, message = "Character must be between 2 and 50 characters long")
	private String character;
	
	

}
