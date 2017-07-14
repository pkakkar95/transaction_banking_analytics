<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Nucleus|Transaction Banking Analytics</title>
<link rel="shortcut icon" type="image/png" href="/favicon.png"/>
<script charset="utf-8" type="text/javascript" src="d3.v3.min.js"></script>
<script charset="utf-8" type="text/javascript" src="c3.min.js"></script>
<link href="style1.css" rel="stylesheet" type="text/css"/>
<link rel="stylesheet" type="text/css" href="style.css">
<script type="text/javascript" src="papaparse.min.js"></script>
<script charset="utf-8" type="text/javascript" src="http://code.jquery.com/jquery-latest.js"></script>
    <style>
.button {
  padding: 15px 25px;
  font-size: 24px;
  text-align: center;
  cursor: pointer;
  outline: none;
  color: #fff;
  background-color: aliceblue;
  border: none;
  border-radius: 15px;
  box-shadow: 0 9px #999;
  position: relative;
  top:59px;
  right:-694px;
}

.button:hover {background-color: #3e8e41}

.button:active {
  background-color: #3e8e41;
  box-shadow: 0 5px #666;
  transform: translateY(4px);
}
</style> 
<title>Nucleus|Transaction Banking Analytics</title>
</head>
<body>
<script type="text/javascript" src="createpie.js"></script>
<div id="main" display=none>
<div class="wrapper">
  <h1>Transaction Banking Analytics</h1>
  <button class="button"><a href="bar.jsp">Next</a></button>
  <button class="button"><a href="v2.jsp">Back</a></button>
   <div class="half">
    <div class="tab">
      <input id="tab-one" type="checkbox" name="tabs">
      <label for="tab-one">Load Data</label>
      <div class="tab-content">
		<p><a href=#>Click here to upload csv or xlsx file</a></p>
      </div>
    </div>
    <div class="tab">
      <input id="tab-two" type="checkbox" name="tabs">
      <label for="tab-two">Trends/Predictions</label>
      <div class="tab-content">
		<p>forecasting=1.85</p>
      </div>
    </div>
    <div class="tab">
      <input id="tab-three" type="checkbox" name="tabs">
      <label for="tab-three">Probable Defaulters</label>
      <div class="tab-content">
		<p>1.Myntra
		</br>2.Flipkart
		</p>
      </div>
    </div>
     <div class="tab">
      <input id="tab-four" type="checkbox" name="tabs">
      <label for="tab-four">High Traffic Period</label>
      <div class="tab-content">
		<p>1.March
		</br>2.September
		</p>
      </div>
    </div>
     <div class="tab">
      <input id="tab-five" type="checkbox" name="tabs">
      <label for="tab-five">Top Customer</label>
      <div class="tab-content">
		<p>1.Airtel
		</br>2.Maruti</p>
      </div>
    </div>
  </div>
</div>
<div id="box">
<div id="chart"></div>
 </div>
</div> 
</body>
</html>