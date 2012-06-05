function util_ont_redirect() {
	document.getElementById('ont_form').target = 'ont_iframe';
	document.getElementById('ont_form').submit();
}
function util_rules_redirect() {
	document.getElementById('rules_form').target = 'rules_iframe';
	document.getElementById('rules_form').submit();
}