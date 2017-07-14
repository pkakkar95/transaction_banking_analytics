function parseData(createBar){
	Papa.parse("train.csv",{
		download:true,
		complete:function(results){
			createBar(results.data);
		}
    });
}
function createBar(data){

	var chart = c3.generate({
	    data: {
	        columns: [
	['neft',1351.0,37.0,7.0,1.0,80.0],
	['cc',2087.0,365.0,33.0,17.0,30.0],
	['dd',569.0,370.0,42.0,9.0,09.0],
	['rtgs',208.0,320.0,20.0,19.0,5.0],
		        ],
	        type: 'bar'
	    },
	    bar: {
	        width: {
	            ratio: 0.5 // this makes bar width 50% of length between ticks
	        }
	        // or
	        //width: 100 // this makes bar width 100px
	    }
	});



}
parseData(createBar);