var endpoints= {lmdb:"http://localhost:3031/ds/query",
				dbpedia:"http://localhost:3030/ds/query"};

$(document).ready(setup);

function setup() {
	$("#survey").load("html/survey.html", function() { survey_setup(endpoints); });
	$("#probe").load("html/probe.html", function() { probe_setup(endpoints); });
	$("#extract").load("html/extract.html", function() { setup(endpoints); });
	$("#integrate").load("html/integrate.html", function() { int_setup(); });
	$("#analytic").load("html/analytic.html", function() { ana_setup(); });
	$("#tabs").tabs();
}