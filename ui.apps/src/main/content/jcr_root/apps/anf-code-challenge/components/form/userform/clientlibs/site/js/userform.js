/*******************************************************************************
 * Copyright 2016 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
(function($) {
	"use strict";

	$("#userform").submit(function(e) {

		e.preventDefault(); // avoid to execute the actual submit of the form.

		// hide previous messages
		$("#error-message").hide();
		$("#success-message").hide();

		var form = $(this);
		var actionUrl = form.attr('action');

		$.ajax({
			type : "POST",
			url : actionUrl,
			data : form.serialize(), // serializes the form's elements.
			success : function(data) {
				$("#success-message").show();
			},
			error : function(data) {
				$("#error-message").text(data.responseJSON.message);
				$("#error-message").show();
			}
		});

	});

})(jQuery);
