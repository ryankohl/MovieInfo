var integrationService= "http://localhost:8080/MovieInfo/integration";

function int_setup() {
	int_setBehaviors();
	int_getDataFiles();
	int_getOntologyFiles();
	int_getRulesFiles();
}
function int_setBehaviors() {
	$("#integrateSurveyButton").click(int_doSurvey);	
	$("#integrateSelectButton").click(int_doSelectQuery);
	$("#integrateDeleteOntologyButton").click(int_deleteOntologies);
	$("#integrateDeleteRulesButton").click(int_deleteRules);
	$("#addOntologyButton").click(addOntologyButtonBehavior);
	$("#addRulesButton").click(addRulesButtonBehavior);
}
function addOntologyButtonBehavior() {
	$("#ontologyInput").val('');
	int_getOntologyFiles();
}
function addRulesButtonBehavior() {
	$("#rulesInput").val('');
	int_getRulesFiles();
}
function int_doSurvey() {
	$.getJSON(integrationService, {type:'survey'}, int_setSurveyData);	
}
function int_setSurveyData(data) {
	int_setClassList(data);
	$("#surveyClassList option:first").attr('selected','selected');
	int_setClassContent(data);
	$("#integrateClassList").change(function() { int_setClassContent(data); });
}
function int_setClassList(data) {
	$('#integrateClassList').empty();
	var classes= data['classList'].sort();
	$.each(classes, function(i,v) {
		$('#integrateClassList').append('<option>'+v+'</option>');
	});
}
function int_setClassContent(data) {
	var klass= $('#integrateClassList option:selected').val();	
	var content= data['predicateMap'][klass].sort(int_sortClassContent);
	$('#integrateClassContent').empty();
	$.each(content, int_setPredicate);
}
function int_sortClassContent(a, b){
	  var aName = a[0];
	  var bName = b[0]; 
	  return ((aName < bName) ? -1 : ((aName > bName) ? 1 : 0));
}
function int_setPredicate(i, v) {
	$('#integrateClassContent').append('<li>'+v[0]+': '+v[1]+'</li>');
}
function int_doSelectQuery() {
	var query= $('#integrateSelectBox').val();	
	$.getJSON(integrationService, {query:query, type:'query'}, int_showSelectResults);		
}
function int_showSelectResults(data) {	
	$('#integrateSelectResults').dataTable({
		'aaData': data.answers,
		'aoColumns': $.map(data.fields, function(n) { return {'sTitle': n}; }),
		'bDestroy': true,
		"bJQueryUI": true
	});	
	$('#integrateSelectResults').css('width', '100%');
}
function int_getDataFiles() {
	$.getJSON(integrationService, {type:'data'}, int_setDataFiles);
}
function int_setDataFiles(data) {	
	$("#integrateDataFiles").empty();
	$.each(data, function(i,v) {		
		var link= int_getLink("resources/rdf/", v);		
		$("#integrateDataFiles").append("<li>"+ link +"</li>");
	});
}
function int_getOntologyFiles() {
	$.getJSON(integrationService, {type:'ontologies'}, int_setOntologyFiles);
}
function int_setOntologyFiles(data) {
	$("#integrateOntologyFiles").empty();
	$.each(data, function(i, v) {		
		var link= int_getLink("resources/ontology/", v);
		$("#integrateOntologyFiles").append("<option>"+link+"</option>");
	});
}
function int_getRulesFiles() {
	$.getJSON(integrationService, {type:'rules'}, int_setRulesFiles);
}
function int_setRulesFiles(data) {
	$("#integrateRulesFiles").empty();
	$.each(data, function(i,v) {
		var link= int_getLink("resources/rules/", v);
		$("#integrateRulesFiles").append("<option>"+link+"</option>");
	});
}
function int_getLink(dirname, filename) {
	return '<a target="_blank" href="'+dirname+filename+'">'+filename+'</a>';	
}
function int_deleteOntologies() {
	var file= $("#integrateOntologyFiles").val();
	int_deleteFile(file, 'deleteOntology');	
	int_getOntologyFiles();
}
function int_deleteRules() {
	var file= $("#integrateRulesFiles").val();
	int_deleteFile(file, 'deleteRule');		
	int_getRulesFiles();
}
function int_deleteFile(name, type) {
	$.post(integrationService, {filename:name, type:type});
}