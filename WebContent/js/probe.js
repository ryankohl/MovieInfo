var probeService= "http://localhost:8080/MovieInfo/probe";
var probe_data;

function probe_setup(endpoints) {
	probe_getSourceSurvey(endpoints.lmdb);	
}
function probe_getSourceSurvey(endpointURL) {
	$.getJSON(probeService, 
			  {endpoint:endpointURL, type:'survey'},
			  probe_initialize
	);
}
function probe_initialize(d) {
	probe_data= d;
	$('#probeSourceEndpoint').empty().append('<option>lmdb</option>');
	$('#probeTargetEndpoint').empty().append('<option>dbpedia</option>');
	probe_setSourceClasses();
	probe_setBehavior();
	$('#probeSampleSize').val(10);
}
function probe_setSourceClasses() {	
	$('#probeSourceClasses').empty();
	var classes= probe_data.classList.sort();
	$.each(classes, function(i,v) {
		$('#probeSourceClasses').append('<option>'+v+'</option>');
	});
	$("#probeSourceClasses option:first").attr('selected','selected');
}
function probe_setBehavior() {		
	$("#probeButton").click(function() { probe_do(); });
}
function probe_do() {	
	var rdf_type= "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"; 
	var c= $('#probeSourceClasses option:selected').val();	
	var cs= $.grep(probe_data.predicateMap[c], function(v, i) { return (v[0] == rdf_type); })[0];
	$.getJSON(probeService,
			  {type: "probe",
			   source: endpoints.lmdb,
			   target: endpoints.dbpedia,
			   klass: c,
			   classSize: cs[1],
			   sampleSize: $('#probeSampleSize').val()},
			  probe_showResults
	);
}
function probe_showResults(d) {
	$('#probeResults').dataTable({
		'aaData': d.answers,
		'aoColumns': $.map(d.fields, function(n) { return {'sTitle': n}; }),
		'bDestroy': true
	});
	$('#probeResults').css('width', '100%');	
}