var extractService= "http://localhost:8080/MovieInfo/extraction";
var sourceURLs;
var queries;

function setup(endpoints) {
	sourceURLs= endpoints;
	loadDropdown();
	setBehaviors();
	getConstructs();
}
function loadDropdown() {
	$('#extractDropdown').empty();	
	$.each(sourceURLs, function(k,v) {
		$('#extractDropdown').append('<option>'+k+'</option>');
	});
}
function setBehaviors() {
	$("#extractConstructButton").click(queryServer);
	$("#extractShow").click(showConstruct);
	$("#extractSave").click(saveConstruct);
	$("#extractDelete").click(deleteConstruct);
}
function queryServer() {
	var endpoint= sourceURLs[$('#extractDropdown').val()];
	var query= $('#extractConstructBox').val();	
	$.getJSON(extractService, {type:"query", endpoint:endpoint, query:query}, showResults);
}
function showResults(data) {
	$('#extractSurveyResults').dataTable({
		'aaData': data.answers,
		'aoColumns': $.map(data.fields, function(n) { return {'sTitle': n}; }),
		'bDestroy': true
	});
	$('#extractSurveyResults').css('width', '100%');
}
function showConstruct() {
	var construct= $("#extractSavedConstructs").val();
	console.log(construct);
	console.log(queries[construct]);
	$("#extractConstructBox").val(queries[construct]);
}
function saveConstruct() {
	var endpoint= sourceURLs[$('#extractDropdown').val()];
	var query= $('#extractConstructBox').val();
	var filename= $('#extractGraphName').val();
	$.post(extractService, 
		   {type:'save', endpoint:endpoint, query:query, filename:filename}, 
		   updateConstructs);
}
function deleteConstruct() {
	var filename= $('#extractSavedConstructs').val();
	$.post(extractService, 
		   {type:'delete', filename:filename}, 
		   updateConstructs);
}
function updateConstructs() {
	   getConstructs();
	   int_getDataFiles();
	   $('#extractGraphName').val('');
}
function getConstructs() {
	$.getJSON(extractService, {type:"constructs"}, setConstructs);
}
function setConstructs(data) {
	queries= data;
	$("#extractSavedConstructs").empty();
	$.each(data, appendConstruct);
}
function appendConstruct(k, v) {
	$('#extractSavedConstructs').append('<option>'+k+'</option>');	
}