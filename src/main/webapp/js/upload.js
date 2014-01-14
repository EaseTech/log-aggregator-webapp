angular.module('fileUpload', [ 'angularFileUpload' ]);

var MyCtrl = [ '$scope', '$http', '$timeout', '$upload','$location', '$anchorScroll',  function($scope, $http, $timeout, $upload , $location, $anchorScroll) {
	$scope.fileReaderSupported = window.FileReader != null;
	$scope.uploadRightAway = true;
	$scope.availableReports = [];
	$scope.env = [];
	$scope.components = [];
	
	$scope.apps = [];
	$scope.availableReports = [];
	$scope.filePath = "Enter file path";
	$scope.exprPlaceHolder= "Enter an Expr to search" ;
	$scope.uniqueExceptions = [];
	$scope.exceptionsCount = [];
	$scope.fetching = "";
	$scope.requestIdData = [];
	$scope.regularExpressionData = [];
	$scope.exceptionTraceData = [];
	$scope.envList = null;
	$scope.changeAngularVersion = function() {
		window.location.hash = $scope.angularVersion;
		window.location.reload(true);
	}
	$scope.hasUploader = function(index) {
		return $scope.upload[index] != null;
	};
	$scope.abort = function(index) {
		$scope.upload[index].abort(); 
		$scope.upload[index] = null;
	};
	$scope.angularVersion = window.location.hash.length > 1 ? window.location.hash.substring(1) : '1.2.0';
	$scope.onFileSelect = function($files) {
		$scope.selectedFiles = [];
		$scope.progress = [];
		if ($scope.upload && $scope.upload.length > 0) {
			for (var i = 0; i < $scope.upload.length; i++) {
				if ($scope.upload[i] != null) {
					$scope.upload[i].abort();
				}
			}
		}
		$scope.upload = [];
		$scope.uploadResult = [];
		$scope.selectedFiles = $files;
		$scope.dataUrls = [];
		for ( var i = 0; i < $files.length; i++) {
			var $file = $files[i];
			if (window.FileReader && $file.type.indexOf('image') > -1) {
			  	var fileReader = new FileReader();
		        fileReader.readAsDataURL($files[i]);
		        function setPreview(fileReader, index) {
		            fileReader.onload = function(e) {
		                $timeout(function() {
		                	$scope.dataUrls[index] = e.target.result;
		                });
		            }
		        }
		        setPreview(fileReader, i);
			}
			$scope.progress[i] = -1;
			if ($scope.uploadRightAway) {
				$scope.start(i);
			}
		}
	}
	
	$scope.start = function(index) {
		$scope.progress[index] = 0;
		if ($scope.howToSend == 1) {
			$scope.upload[index] = $upload.upload({
				url : 'log/uploadFile',
				method: $scope.httpMethod,
				headers: {'myHeaderKey': 'myHeaderVal'},
				file: $scope.selectedFiles[index],
				fileFormDataName: 'file'
			}).then(function(response) {
				var data = response.data
				$scope.exceptionTraceData = [];
		  		$scope.availableReports = [];
		  		$scope.env = [];
		  		$scope.components = [];
		  		$scope.apps = [];
		  		var envNames = data.envNames;
		  		var componentNames = data.componentNames;
		  		var reports = data.reports;
		  		var appNames = data.applicationNames;
		  		
		  		for(var i=0 ; i<envNames.length ; i++) {
		  			$scope.env.push(envNames[i]);
		  		}
		  		
		  		
		  		angular.forEach(reports, function(report) {
		  			$scope.availableReports.push({name:report.name, value:report.description, selected:report.selected});
		  	    });
		  		
				$scope.uploadResult.push(response.data.result);
			}, null, function(evt) {
				$scope.progress[index] = parseInt(100.0 * evt.loaded / evt.total);
			});
		} else {
			var fileReader = new FileReader();
	        fileReader.readAsArrayBuffer($scope.selectedFiles[index]);
            fileReader.onload = function(e) {
		        $scope.upload[index] = $upload.http({
		        	url: 'upload',
					headers: {'Content-Type': $scope.selectedFiles[index].type},
					data: e.target.result
				}).then(function(response) {
					$scope.uploadResult.push(response.data.result);
				}, null, function(evt) {
					// Math.min is to fix IE which reports 200% sometimes
					$scope.progress[index] = Math.min(100, parseInt(100.0 * evt.loaded / evt.total));
				});
            }
		}
	}
	
	$scope.goUp = function() {
		$location.hash("startSection");
  		$anchorScroll();
	}
	$scope.fetchLogInformation = function() {
		  $scope.filePath = $scope.enteredFilePath;
		  var query = "log/filePath?filepath="+ $scope.filePath;
		  $location.hash("resultsSection");
	  		$anchorScroll();
		  $http
		  	.get(query)
		  	.success(function(data) {
		  		$scope.availableReports = [];
		  		var envNames = data.envNames;
		  		var componentNames = data.componentNames;
		  		var reports = data.reports;
		  		var appNames = data.applicationNames;
		  		$scope.exceptionTraceData = [];
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
		  		$scope.fetching = data;

		  	});
	  };
	  $scope.fetchUniqueExceptions = function(selectedReport) {
		  var envName = $scope.envName;
		  var compNames = $scope.components;
		  var selectedComponents = "";
		  var appName = $scope.appName;
		  $scope.requestIdData = [];
		  $scope.uniqueExceptions = [];
		  $scope.exceptionTraceData = [];
		  $scope.exceptionsCount = [];
		  $scope.error = "";
		  angular.forEach(compNames, function(compName) {
		      var componentSelected = compName.selected ? compName.name : null;
		      if(componentSelected != null) {
		    	  selectedComponents = selectedComponents + componentSelected+",";
		      }
		    });
		  if(envName==undefined) {
			  $scope.error = "Please select an Environment Name";
			  return;
		  }
		  if(appName==undefined) {
			  $scope.error = "Please select an Application Name";
			  return;
		  }
		  if(selectedComponents=="") {
			  $scope.error = "Please select Components Name";
			  return;
		  }
		  $scope.fetching = "Fetching information for " + selectedReport + " Report.....";
		  var query = "log/fetchLogs?envName="+envName+"&appName="+appName+"&compName="+selectedComponents+"&filepath="+ $scope.filePath +"&reports="+selectedReport+"&showCachedData="+$scope.cacheData;
		  $location.hash("resultsSection");
	  		$anchorScroll();
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
		  		$scope.fetching = data;

		  	});
		  
	  };
	  
	  $scope.fetchExceptionCount = function(selectedReport) {
		  var envName = $scope.envName;
		  var compNames = $scope.components;
		  var selectedComponents = "";
		  var appName = $scope.appName;
		  $scope.requestIdData = [];
		  $scope.uniqueExceptions = [];
		  $scope.exceptionsCount = [];
		  $scope.exceptionTraceData = [];
		  $scope.error = "";
		  angular.forEach(compNames, function(compName) {
		      var componentSelected = compName.selected ? compName.name : null;
		      if(componentSelected != null) {
		    	  selectedComponents = selectedComponents + componentSelected+",";
		      }
		    });
		  
		  if(envName==undefined) {
			  $scope.error = "Please select an Environment Name";
			  return;
		  }
		  if(appName==undefined) {
			  $scope.error = "Please select an Application Name";
			  return;
		  }
		  if(selectedComponents=="") {
			  $scope.error = "Please select Components Name";
			  return;
		  }
		  $scope.fetching = "Fetching information for " + selectedReport + " Report.....";
		  var query = "log/fetchExceptionCountLogs?envName="+envName+"&appName="+appName+"&compName="+selectedComponents+"&filepath="+ $scope.filePath +"&reports="+selectedReport+"&showCachedData="+$scope.cacheData;
		  $location.hash("resultsSection");
	  	  $anchorScroll();
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
		  		$scope.fetching = data;
		  	});
		  
	  };

	  $scope.fetchExprInformation = function() {
		  var envName = $scope.envName;
		  var compNames = $scope.components;
		  var selectedComponents = "";
		  var appName = $scope.appName;
		  var expr = $scope.expr;
		  var isRegularExpr = $scope.isRegExpr;
		  
		  $scope.uniqueExceptions = [];
		  $scope.exceptionsCount = [];
		  $scope.regularExpressionData =[];
		  $scope.requestIdData =[];
		  $scope.exceptionTraceData =[];
		  $scope.errorExpr ="";
		  angular.forEach(compNames, function(compName) {
		      var componentSelected = compName.selected ? compName.name : null;
		      if(componentSelected != null) {
		    	  selectedComponents = selectedComponents + componentSelected+",";
		      }
		    });
		  if(envName==undefined) {
			  $scope.errorExpr = "Please select an Environment Name";
			  return;
		  }
		  if(appName==undefined) {
			  $scope.errorExpr = "Please select an Application Name";
			  return;
		  }
		  if(selectedComponents=="") {
			  $scope.errorExpr = "Please select Components Name";
			  return;
		  }
		  $scope.fetching = "Fetching information for " + expr + " ......";
		  var query;
		  if(isRegularExpr) {
			  query = "log/fetchUsingRegularExpression?envName="+envName+"&appName="+appName+"&compName="+selectedComponents+"&regExpr="+expr+"&showCachedData="+$scope.cacheData;
			  
		  } else {
			  query = "log/fetchUsingConstantExpression?envName="+envName+"&appName="+appName+"&compName="+selectedComponents+"&constantExpr="+expr+"&showCachedData="+$scope.cacheData;
		  }
		  $location.hash("resultsSection");
	  	  $anchorScroll();
		  $http
		  	.get(query)
		  	.success(function(idLogs) {
		  		$scope.fetching = "";
		  		angular.forEach(idLogs, function(idLog) {
		  			$scope.requestIdData.push(idLog);
		  			
		  	    });
		  		
		  		if($scope.requestIdData.length==0) {
		  			$scope.fetching = "No Data Found";
		  		}
		  	})
		  	.error(function(data) {
		  		$scope.fetching = data;
		  	});
		  
	  };
	  
	  $scope.fillApplicationOptions = function() {
		  var envName = $scope.envName;
		  var envList = $scope.envList;
		  angular.forEach(envList, function(env) {
			 if(env.name == envName) {
				 var appList = env.application;
				 angular.forEach(appList, function(app) {
					 $scope.apps.push(app.name);
				 });
			 } 
		  });
		  
	  };
	  
	  $scope.fetchRegExprInformation = function() {
		  var envName = $scope.envName;
		  var compNames = $scope.components;
		  var selectedComponents = "";
		  var appName = $scope.appName;
		  var regExpr = $scope.regExpr;
		 
		  $scope.uniqueExceptions = [];
		  $scope.exceptionsCount = [];
		  $scope.requestIdData =[];
		  $scope.regularExpressionData =[];
		  $scope.exceptionTraceData = [];
		  $scope.errorExpr ="";
		  angular.forEach(compNames, function(compName) {
		      var componentSelected = compName.selected ? compName.name : null;
		      if(componentSelected != null) {
		    	  selectedComponents = selectedComponents + componentSelected+",";
		      }
		    });
		  if(envName==undefined) {
			  $scope.errorExpr = "Please select an Environment Name";
			  return;
		  }
		  if(appName==undefined) {
			  $scope.errorExpr = "Please select an Application Name";
			  return;
		  }
		  if(selectedComponents=="") {
			  $scope.errorExpr = "Please select Components Name";
			  return;
		  }
		  $scope.fetching = "Fetching information for " + regExpr + " ......";
		  var query = "log/fetchUsingRegularExpression?envName="+envName+"&appName="+appName+"&compName="+selectedComponents+"&regExpr="+regExpr+"&showCachedData="+$scope.cacheData;
		  $location.hash("resultsSection");
	  	  $anchorScroll();
		  $http
		  	.get(query)
		  	.success(function(idLogs) {
		  		$scope.fetching = "";
		  		angular.forEach(idLogs, function(idLog) {
		  			$scope.regularExpressionData.push(idLog);
		  			
		  	    });
		  		
		  		if($scope.regularExpressionData.length==0) {
		  			$scope.fetching = "No Data Found";
		  		}
		  	})
		  	.error(function(data) {
		  		$scope.fetching = data;
		  	});
		  
	  };
	  
	  $scope.fetchComponentNames = function() {
		  $scope.components = [];
		  $scope.uniqueExceptions = [];
		  $scope.exceptionsCount = [];
		  $scope.requestIdData =[];
		  $scope.exceptionTraceData = [];
		  $scope.regularExpressionData =[];
		  var envName = $scope.envName;
		  var appName = $scope.appName;
		  $scope.errorTrace ="";
		  if(envName==undefined) {
			  $scope.errorTrace = "Please select an Environment Name";
			  return;
		  }
		  if(appName==undefined) {
			  $scope.errorTrace = "Please select an Application Name";
			  return;
		  }
		  var query = "log/fetchComponentNames?envName="+envName+"&appName="+appName;
		  
		  $http
		  	.get(query)
		  	.success(function(componentNames) {
		  		$scope.fetching = "";
		  		angular.forEach(componentNames, function(componentName) {
		  			
		  			$scope.components.push({name:componentName , selected:false});
		  			
		  	    });
		  		
		  		if($scope.components.length==0) {
		  			$scope.fetching = "No Data Found";
		  		}
		  	})
		  	.error(function(data) {
		  		$scope.fetching = data;
		  	});
	  };
	  
	  $scope.fetchApplicationNames = function() {
		  $scope.components = [];
		  $scope.apps = [];
		  $scope.uniqueExceptions = [];
		  $scope.exceptionsCount = [];
		  $scope.requestIdData =[];
		  $scope.exceptionTraceData = [];
		  $scope.regularExpressionData =[];
		  var envName = $scope.envName;
		  $scope.errorTrace ="";
		  if(envName==undefined) {
			  $scope.errorTrace = "Please select an Environment Name";
			  return;
		  }
		  
		  var query = "log/fetchApplicationNames?envName="+envName;
		  
		  $http
		  	.get(query)
		  	.success(function(appNames) {
		  		$scope.fetching = "";
		  		angular.forEach(appNames, function(appName) {
		  			
		  			$scope.apps.push(appName);
		  			
		  	    });
		  		
		  		if($scope.apps.length==0) {
		  			$scope.fetching = "No Data Found";
		  		}
		  		if($scope.apps.length==1) {
		  			$scope.appName = $scope.apps[0];
		  			$scope.fetchComponentNames();
		  		}
		  	})
		  	.error(function(data) {
		  		$scope.fetching = data;
		  	});
	  }
	  $scope.fetchExceptionTrace = function() {
		  var envName = $scope.envName;
		  var compNames = $scope.components;
		  var selectedComponents = "";
		  var appName = $scope.appName;
		  var exceptionName = $scope.exceptionName;
		  var startDate = $scope.startDate;
		  var endDate = $scope.endDate;
		  var linesBefore = $scope.linesBefore;
		  var linesAfter = $scope.linesAfter;
		  
		  $scope.uniqueExceptions = [];
		  $scope.exceptionsCount = [];
		  $scope.requestIdData =[];
		  $scope.exceptionTraceData = [];
		  $scope.regularExpressionData =[];
		  $scope.errorTrace ="";
		  angular.forEach(compNames, function(compName) {
		      var componentSelected = compName.selected ? compName.name : null;
		      if(componentSelected != null) {
		    	  selectedComponents = selectedComponents + componentSelected+",";
		      }
		    });
		  if(envName==undefined) {
			  $scope.errorTrace = "Please select an Environment Name";
			  return;
		  }
		  if(appName==undefined) {
			  $scope.errorTrace = "Please select an Application Name";
			  return;
		  }
		  if(selectedComponents=="") {
			  $scope.errorTrace = "Please select Components Name";
			  return;
		  }
		  if(startDate == undefined) {
			  startDate = "";
		  }
		  if(endDate == undefined) {
			  endDate == "";
		  }
		  if(linesBefore == undefined) {
			  linesBefore = 0;
		  }
		  if(linesAfter == undefined) {
			  linesAfter = 100;
		  }
		  $scope.fetching = "Fetching stack trace for " + exceptionName + " Env : " + envName + " App :" + appName + " components :" + selectedComponents + ". it MAY take a while.";
		  
		  var query = "log/fetchExceptionTrace?envName="+envName+"&appName="+appName+"&compName="+selectedComponents+"&expr="+exceptionName+"&startDate="+startDate+"&endDate="+endDate+"&linesBefore="+linesBefore+"&linesAfter="+linesAfter+"&showCachedData="+$scope.cacheData;
		  $location.hash("resultsSection");
	  	  $anchorScroll();
		  $http
		  	.get(query)
		  	.success(function(grepResults) {
		  		$scope.fetching = "";
		  		angular.forEach(grepResults, function(grepResult) {
		  			
		  			$scope.exceptionTraceData.push(grepResult);
		  			
		  	    });
		  		
		  		if($scope.exceptionTraceData.length==0) {
		  			$scope.fetching = "No Data Found";
		  		}
		  	})
		  	.error(function(data) {
		  		$scope.fetching = data;
		  	});
		  
	  };
	  
	  var init = function() {
		  var query = "log/init";
		  
		  $http
		  	.get(query)
		  	.success(function(data) {
		  		$scope.availableReports = [];
		  		$scope.env = [];
		  		$scope.components = [];
		  		var envNames = data.envNames;
		  		var componentNames = data.componentNames;
		  		var reports = data.reports;
		  		var appNames = data.applicationNames;
		  		
		  		for(var i=0 ; i<envNames.length ; i++) {
		  			$scope.env.push(envNames[i]);
		  		}
		  		angular.forEach(reports, function(report) {
		  			$scope.availableReports.push({name:report.name, value:report.description, selected:report.selected});
		  	    });
		  		
			  
		  	}).error(function(data) {
		  		$scope.fetching = data;

		  	});
	  };
	  init();
} ];
