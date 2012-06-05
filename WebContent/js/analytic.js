var analyticService= "http://localhost:8080/MovieInfo/analytic";
var data;

function ana_setup() {
	$.getJSON(analyticService, initialize);
}
function initialize(d) {
	data= d;
	if (data.items.length != 0) { ana_populateDropdown(data.items); }
}
function ana_populateDropdown(items) {
	ana_setItemsList(items);
	$("#analyticItemsList option:first").attr('selected','selected');
	ana_onChange();
	$("#analyticItemsList").change(function() { ana_onChange(); });
}
function ana_populatePies(item, pies) {
	ana_setPieList(pies);
	$("#analyticPiesList option:first").attr('selected','selected');
	ana_pieChange(item);
	$("#analyticPiesList").change(function() { ana_pieChange(item); });
}
function ana_setPieList(pies) {
	$('#analyticPiesList').empty();	
	var sortedPies= pies.sort();
	$.each(sortedPies, function(i,v) {		
		$('#analyticPiesList').append('<option>'+v+'</option>');
	});
}
function ana_pieChange(item) {
	var pie= $('#analyticPiesList option:selected').val();
	ana_populatePieChart(item, pie);
}
function ana_setItemsList(items) {
	$('#analyticItemsList').empty();	
	var itemNames= $.map(items, function(i) { return data.ids[i].name[0]; });
	var sortedItemNames= itemNames.sort();
	$.each(sortedItemNames, function(i,v) {		
		$('#analyticItemsList').append('<option>'+v+'</option>');
	});
}
function ana_onChange() {	
	var itemName= $('#analyticItemsList option:selected').val();
	var item= data.names[itemName].id[0];
	ana_populateIdentifying(item);
	ana_populateActivity(item);
	ana_populateAssociate(item);	
	ana_populatePies(item, data.pies); 
}
function ana_populateIdentifying(item) {	
	$("#analyticIdentifying").empty();
	var d= data.identifying[item];	
	if (!($.isEmptyObject(d))) {
		$.each(d, function(k,v) {						
			$.each(v, function(i, value) {				
				$("#analyticIdentifying").append("<p>"+k+": "+value+"</p>");	
			});			
		}); 
	}
}
function ana_populateActivity(item) {
	$("#analyticActivityBody").empty();
	var d= data.activity[item];		
	if (!($.isEmptyObject(d))) {
		$.each(d, function(k,v) {			
			$.each(v, function(i, value) {				
				$("#analyticActivityBody").append("<p>"+k+": "+value+"</p>");	
			});			
		}); 
	}
}
function ana_populateAssociate(item) {
	$("#analyticAssociateBody").empty();
	var d= data.associate[item];	
	if (!($.isEmptyObject(d))) {
		$.each(d, function(k,v) {						
			$.each(v, function(i, value) {				
				$("#analyticAssociateBody").append("<p>"+k+": "+value+"</p>");	
			});			
		}); 
	}
}
function ana_populatePieChart(item, pie) {	
	$("#analyticPieChart").empty();	
	try { 
		var d= data.piechart[item][pie];	
		drawPieChart(d); 
	}
	catch(err) {}
}
function drawPieChart(d) {
	$.jqplot('analyticPieChart', [d], {
		gridPadding: {top:0, bottom:38, left:0, right:0},
		seriesDefaults:{
			renderer:$.jqplot.PieRenderer, 
			trendline:{ show:false }, 
			rendererOptions: { padding: 8, showDataLabels: true }
		},
		legend:{
			show:true, 
			placement: 'outside', 
			rendererOptions: {
				numberRows: 2
			}, 
			location:'s',
			marginTop: '15px'
		}       
	});
}