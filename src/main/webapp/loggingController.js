function loggingController($scope , $http , $upload) {
	$scope.env = [];
	$scope.apps = [];
	$scope.components = [];
	$scope.filePath = "Enter file path";
	$scope.requestIdPlaceHolder= "Enter Request Id";
	$scope.availableReports = [];
	$scope.uniqueExceptions = [];
	$scope.exceptionsCount = [];
	$scope.fetching = "";
	$scope.requestIdData = [];
	
	$scope.logLines = [];
	

	$scope.onFileSelect = function($files) {
		
		  
		var file = $files[0];
		$scope.upload = $upload.upload({
			url:'log/uploadFile',
			file:file,
			
		}).progress(function(evt){
			
		}).success(function(data , status, headers, config) {
			alert(data);
	  		$scope.availableReports = [];
	  		var envNames = data.envNames;
	  		var componentNames = data.componentNames;
	  		var reports = data.reports;
	  		var appNames = data.applicationNames;
	  		$scope.env = [];
	  		$scope.components = [];
	  		for(var i=0 ; i<envNames.length ; i++) {
	  			$scope.env.push(envNames[i]);
	  		}
	  		for(var i=0 ; i<componentNames.length ; i++) {
	  			var compName = componentNames[i]
	  			$scope.components.push({name:compName , selected:false});
	  		}
	  		for(var i=0 ; i<appNames.length ; i++) {
	  			$scope.apps.push(appNames[i]);
	  		}
	  		
	  		angular.forEach(reports, function(report) {
	  			$scope.availableReports.push({name:report.name, value:report.description, selected:report.selected});
	  	    });
		});
		  
		 
	  };
	  
	  
 
  $scope.fetchLogInformation = function() {
	  $scope.filePath = $scope.enteredFilePath;
	  var query = "log/filePath?filepath="+ $scope.filePath;
	  
	  $http
	  	.get(query)
	  	.success(function(data) {
	  		alert(data);
	  		$scope.availableReports = [];
	  		var envNames = data.envNames;
	  		var componentNames = data.componentNames;
	  		var reports = data.reports;
	  		var appNames = data.applicationNames;
	  		$scope.env = [];
	  		$scope.components = [];
	  		for(var i=0 ; i<envNames.length ; i++) {
	  			$scope.env.push(envNames[i]);
	  		}
	  		for(var i=0 ; i<componentNames.length ; i++) {
	  			var compName = componentNames[i]
	  			$scope.components.push({name:compName , selected:false});
	  		}
	  		for(var i=0 ; i<appNames.length ; i++) {
	  			$scope.apps.push(appNames[i]);
	  		}
	  		
	  		angular.forEach(reports, function(report) {
	  			$scope.availableReports.push({name:report.name, value:report.description, selected:report.selected});
	  	    });
	  		
		  
	  	}).error(function(data) {
	  		alert("An error occurred");

	  	});
  };
  
  $scope.fetchUniqueExceptions = function(selectedReport) {
	  var envName = $scope.envName;
	  var compNames = $scope.components;
	  var selectedComponents = "";
	  var appName = $scope.appName;
	  $scope.fetching = "Fetching information for " + selectedReport + " Report.....";
	  $scope.uniqueExceptions = [];
	  $scope.exceptionsCount = [];
	  angular.forEach(compNames, function(compName) {
	      var componentSelected = compName.selected ? compName.name : null;
	      if(componentSelected != null) {
	    	  selectedComponents = selectedComponents + componentSelected+",";
	      }
	    });
	  var query = "log/fetchLogs?envName="+envName+"&appName="+appName+"&compName="+selectedComponents+"&filepath="+ $scope.filePath +"&reports="+selectedReport;

	  $http
	  	.get(query)
	  	.success(function(uniqueExceptions) {
	  		$scope.fetching = "";
	  		angular.forEach(uniqueExceptions, function(uniqueException) {
	  			var exceptionStrings = uniqueException.uniqueExceptions;
	  			var logLines = [];
	  			angular.forEach(exceptionStrings, function(exceptionStr) {
	  				var exStr = exceptionStr.string;
	  				logLines.push(exStr);
	  			});
	  			$scope.uniqueExceptions.push({description:uniqueException.description , compName:uniqueException.componentName , logLines: logLines});
	  	    });
	  		if($scope.uniqueExceptions.length==0) {
	  			$scope.fetching = "No Data Found";
	  		}
		  
	  	})
	  	.error(function(data) {
	  		$scope.fetching = "Error occured while trying to fetch the data";

	  	});
	  
  };
  
  $scope.fetchExceptionCount = function(selectedReport) {
	  var envName = $scope.envName;
	  var compNames = $scope.components;
	  var selectedComponents = "";
	  var appName = $scope.appName;
	  $scope.fetching = "Fetching information for " + selectedReport + " Report.....";
	  $scope.uniqueExceptions = [];
	  $scope.exceptionsCount = [];
	  angular.forEach(compNames, function(compName) {
	      var componentSelected = compName.selected ? compName.name : null;
	      if(componentSelected != null) {
	    	  selectedComponents = selectedComponents + componentSelected+",";
	      }
	    });
	  var query = "log/fetchExceptionCountLogs?envName="+envName+"&appName="+appName+"&compName="+selectedComponents+"&filepath="+ $scope.filePath +"&reports="+selectedReport;

	  $http
	  	.get(query)
	  	.success(function(uniqueExceptions) {
	  		$scope.fetching = "";
	  		angular.forEach(uniqueExceptions, function(uniqueException) {
	  			var exceptionStrings = uniqueException.uniqueExceptions;
	  			var logExceptionCount = [];
	  			angular.forEach(exceptionStrings, function(exceptionStr) {
	  				var exStr = exceptionStr.exception;
	  				var count = exceptionStr.count;
	  				logExceptionCount.push({statement:exStr , count:count});
	  			});
	  			$scope.exceptionsCount.push({description:uniqueException.description , compName:uniqueException.componentName , logExceptionCount: logExceptionCount});
	  	    });
	  		
	  		if($scope.exceptionsCount.length==0) {
	  			$scope.fetching = "No Data Found";
	  		}
	  	})
	  	.error(function(data) {
	  		$scope.fetching = "Error occured while trying to fetch the data";
	  	});
	  
  };

  $scope.fetchRequestIdInformation = function() {
	  var envName = $scope.envName;
	  var compNames = $scope.components;
	  var selectedComponents = "";
	  var appName = $scope.appName;
	  var requestId = $scope.requestId
	  $scope.fetching = "Fetching information for " + requestId + " ......";
	  $scope.uniqueExceptions = [];
	  $scope.exceptionsCount = [];
	  angular.forEach(compNames, function(compName) {
	      var componentSelected = compName.selected ? compName.name : null;
	      if(componentSelected != null) {
	    	  selectedComponents = selectedComponents + componentSelected+",";
	      }
	    });
	  var query = "log/fetchRequestIdLogs?envName="+envName+"&appName="+appName+"&compName="+selectedComponents+"&filepath="+ $scope.filePath +"&requestId="+requestId;
	  
	  alert(query);
	  $http
	  	.get(query)
	  	.success(function(idLogs) {
	  		$scope.fetching = "";
	  		angular.forEach(idLogs, function(idLog) {
	  			alert(idLog);
	  			$scope.requestIdData.push(idLog);
	  			
	  	    });
	  		
	  		if($scope.requestIdData.length==0) {
	  			$scope.fetching = "No Data Found";
	  		}
	  	})
	  	.error(function(data) {
	  		$scope.fetching = "Error occured while trying to fetch the data";
	  	});
	  
  };
  loggingController.$inject = ['$scope', '$http' , '$upload'];
}