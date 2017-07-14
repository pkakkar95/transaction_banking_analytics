function parseData(createPie){
	Papa.parse("pie_chart.csv",{
		download:true,
		complete:function(results){
			createPie(results.data);
		}
    });
}
function createPie(data){
	var Customer=[];
	var Transaction=[];
	for(var i=1;i<data.length;i++){
		Customer.push(data[i][0]);
		Transaction.push(data[i][1]);
	}
	console.log(Customer);
	console.log(Transaction);

	var chart = c3.generate({
		size: {
	        height: 600,
	        width: 400
	    },
		data: {
	    	url:'pie_chart.csv',
	        columns: [
	        ],
	        type : 'donut',
	        onclick: function (d, i) { console.log("onclick", d, i); },
	        onmouseover: function (d, i) { console.log("onmouseover", d, i); },
	        onmouseout: function (d, i) { console.log("onmouseout", d, i); }
	    },
	    donut: {
	        title: "Transaction share"
	    }
	});
}
parseData(createPie);