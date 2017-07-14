function parseData(createGraph){
	Papa.parse("user2.csv",{
		download:true,
		complete:function(results){
			createGraph(results.data);
		}
    });
}
function createGraph(data){
	var Month=[];
	var Payment=["Transactions"];
	for(var i=1;i<data.length-1;i++){
		Month.push(data[i][0]);
		Payment.push(data[i][3]);
	}
	console.log(Month);
	console.log(Payment);
	var chart = c3.generate({
		bindto:'#chart',
		data: {
	        columns: [
	          Payment  
	        ]
	    },
	   
	    axis: {
	        x: {
	            type: 'category',
	            categories: Month,
	            tick: {
	                multiline:false,
	            	culling: {
	                    max: 15 
	                }
	              
	            }
	        }
	    },
	    zoom: {
	        enabled: true
	    },
	    legend: {
	        position: 'right'
	    },
	    subchart: {
	        show: true
	    }
	    
	});
	
}
parseData(createGraph);