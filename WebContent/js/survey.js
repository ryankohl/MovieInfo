var surveyService= "http://localhost:8080/MovieInfo/survey";
var surveyEndpoints= {};
var surveys= {};

function sortClassContent(a, b){
	  var aName = a[0];
	  var bName = b[0]; 
	  return ((aName < bName) ? -1 : ((aName > bName) ? 1 : 0));
	}
function survey_setup(endpoints) {
	$.each(endpoints, getSurveyData);	
	$("#surveyEndpoints").change(showSurveyData);	
}
function getSurveyData(endpointName, endpointURL) {
	$.getJSON(surveyService, 
			  {endpoint:endpointURL, type:'survey'}, 
			  function(data) { 
				  surveys[endpointName]= data;  
				  surveyEndpoints[endpointName]= endpointURL; 
				  survey_addEndpoint(endpointName);
				  $("#surveyEndpoints").val(endpointName);
				  setSurveyData(endpointName, data);
			  }
	);
}
function survey_addEndpoint(endpointName) {
	$("#surveyEndpoints").append("<option>"+endpointName+"</option>");
}
function showSurveyData() {
	var endpoint= $('#surveyEndpoints option:selected').val();	
	var data= surveys[endpoint];
	setSurveyData(endpoint, data);
}
function setSurveyData(endpoint, data) {			
	setCounts(data);
	setClassList(data);
	setBehavior(data);
	setInitialState();
	setClassContent();
}
function setCounts(data) {
	$('#surveyClassInfo').empty().append("<p>Classes: "+data['classCount']+"</p>");
	$('#surveyPropInfo').empty().append("<p>Properties: "+data['propCount']+"</p>");
}
function setClassList(data) {
	$('#surveyClassList').empty();
	var classes= data['classList'].sort();
	$.each(classes, function(i,v) {
		$('#surveyClassList').append('<option>'+v+'</option>');
	});
}
function setBehavior(data) {
	$("#surveyClassList").change(function() { setClassContent(); });	
	$("#surveyQueryButton").click(doSurveyQuery);
}
function setInitialState() {
	$("#surveyClassList option:first").attr('selected','selected');
}
function setClassContent() {
	var endpoint= $('#surveyEndpoints option:selected').val();	
	var data= surveys[endpoint];
	var klass= $('#surveyClassList option:selected').val();		
	var content= data.predicateMap[klass].sort(sortClassContent);
	$('#surveyClassContent').empty();
	$.each(content, setPredicate);
}
function setPredicate(i, v) {
	$('#surveyClassContent').append('<li>'+v[0]+': '+v[1]+'</li>');
}
function doSurveyQuery() {
	var endpoint= surveyEndpoints[$('#surveyEndpoints option:selected').val()];	
	var query= $('#surveyQueryBox').val();	
	$.getJSON(surveyService, {endpoint:endpoint, query:query, type:'query'}, showSurveyQueryResults);	
}
function showSurveyQueryResults(data) {
	$('#surveyQueryResults').dataTable({
		'aaData': data.answers,
		'aoColumns': $.map(data.fields, function(n) { return {'sTitle': n}; }),
		'bDestroy': true
	});
	$('#surveyQueryResults').css('width', '100%');	
}