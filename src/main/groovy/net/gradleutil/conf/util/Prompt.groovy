package net.gradleutil.conf.util

import groovy.swing.SwingBuilder

class Prompt {

	static String prompt(String prompt, boolean isPassword = false) {
		String response
		if (System.console() == null) {
			try {
				response = dialogPrompt(prompt, isPassword)
			} catch (Exception e) {
				try {
					print "${prompt}: "
					System.out.flush()
					response = System.in.newReader().readLine()
				} catch (Exception ex) {
					System.err.println("Could not prompt for `${prompt}`, returning empty string")
					return ""
				}
			}
		} else {
			Console console = System.console()
			String readResponse = console.readline("\n${prompt}")
			response = new String(readResponse)
		}

		if (response?.size() <= 0) {
			throw new Exception("You MUST enter a value for '${prompt}' to proceed!")
		}
		return response
	}


	static String dialogPrompt(String prompt, boolean isPassword = false) {
		String response
		new SwingBuilder().edt {
			dialog(modal: true, // Otherwise the build will continue running before you closed the dialog
					title: prompt, // Dialog title
					alwaysOnTop: true, // pretty much what the name says
					resizable: false, // Don't allow the user to resize the dialog
					locationRelativeTo: null, // Place dialog in center of the screen
					pack: true, // We need to pack the dialog (so it will take the size of it's children)
					show: true // Let's show it
			) {
				vbox {
					// Put everything below each other
					label(text: prompt + ": ")
					input = isPassword ? passwordField() : textField()
					button(defaultButton: true, text: 'OK', actionPerformed: {
						//                                                              String value = isPassword ? input.password : input.text
						String value = input.text
						response = new String(value)
						dispose()
					})
				}
			}
		}
		return response

	}

}

