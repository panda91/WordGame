/*global define */

'use strict';

define(function() {

	/* Controllers */

	var controllers = {};

	controllers.AppController = function($scope, $http, $document) {

		$scope.words = {};
		$scope.completeWords = [];
		$scope.errors = [];
		$scope.point = 0;

		$scope.init = function() {
			getWord();
			$document.on("keydown", keyHandler);
		};

		$scope.closeAlert = function(index) {
			$scope.errors.splice(index, 1);
		};

		$scope.$on('$destroy', function() {
			$document.off('keydown', keyHandler);
		});

		function keyHandler(event) {
			if (event.keyCode == 13) {
				checkWord();
			} else if (event.keyCode == 8) {
				if($scope.words.answerWord.length > 0){
					$scope.words.answerWord = $scope.words.answerWord.slice(0, -1);
				}
			} else {
				var regex = /^[a-zA-Z]$/;
				var key = event.key;
				if (regex.test(key)) {
					if ($scope.words.word.indexOf(key) != -1) {
						$scope.words.answerWord += key;
					} /*else {
						$scope.errors
								.push({
									type : "warning",
									message : "Word don't have this character! Please try again."
								});
					}*/
				}
			}
			$scope.$apply();
			event.preventDefault();
		}

		function getWord() {
			$http({
				method : 'GET',
				url : '/getWord',
				responseType : 'json',
				dataType : 'json',
				headers : {
					'Content-Type' : 'application/json;charset=UTF-8',
					'accept' : 'application/json'
				}
			}).then(success, error);
		}

		function checkWord() {
			if ($scope.words.answerWord) {
				// $scope.words.answerWord = $scope.answer;
				$http({
					method : 'POST',
					url : '/checkWord',
					data : JSON.stringify($scope.words),
					responseType : 'json',
					dataType : 'json',
					headers : {
						'Content-Type' : 'application/json;charset=UTF-8',
						'accept' : 'application/json'
					}
				}).then(function(res) {
					$scope.words.answerWord = '';
					if (res.data) {
						if (res.data.status == "passed") {
							getDependentWords(res.data.items);
						} else if (res.data.status == "reload") {
							getWords(res.data.items);

							getDependentWords(res.data.items.dependents);
						}
						
						if(res.data.point){
							$scope.point = res.data.point;
						}
					}
				}, error);
			}
		}

		function success(response) {
			var data = response.data;
			if (data && data.word && data.dependents) {
				getWords(data);

				getDependentWords(data.dependents);

			}else{
				$scope.errors.push({
					type : "warning",
					message : "Seem don't have data right now! Please try again later."
				});
			}
		}

		function getWords(data) {
			var length = data.word.length;
			$scope.words = {
				word : [],
				answerWord : ""
			};

			for (var int = 0; int < length; int++) {
				$scope.words.word.push(data.word.charAt(int));
			}
		}

		function getDependentWords(dependents) {
			$scope.completeWords = [];
			var lengthDepend = dependents.length;
			for (var j = 0; j < lengthDepend; j++) {
				var char = [];
				var subLength = dependents[j].length;
				for (var k = 0; k < subLength; k++) {
					char.push(dependents[j].charAt(k));
				}
				$scope.completeWords.push(char);
			}
		}

		function error(response) {

			$scope.errors.push({
				type : "danger",
				status : response.status,
				statusText : response.statusText,
				message : response.data.message
			});
			
			if(response.data.point != undefined){
				$scope.point = response.data.point;
			}
		}
	};
	controllers.AppController.$inject = [ '$scope', '$http', '$document' ];
	
	

	controllers.WithDrawnCtrl = function($scope, $http) {
		$scope.missedWords = [];
		$scope.notAnswer = 0;
		$scope.completeWords = [];
		$scope.correctedWords = 0;
		$scope.errors = [];
		$scope.point = 0;
		
		$scope.init = function(){
			$http({
				method : 'GET',
				url : '/withdrawn',
				responseType : 'json',
				dataType : 'json',
				headers : {
					'Content-Type' : 'application/json;charset=UTF-8',
					'accept' : 'application/json'
				}
			}).then(success, error);
		};
		
		function success(res){
			if(res.data){
				var answer = res.data.answers;
				var missedWords = res.data.missedWord;
				
				var wordLength = missedWords.dependents.length;
				
				if(res.data.point){
					$scope.point = res.data.point;
				}
				
				for (var j = 0; j < wordLength; j++) {
					var char = [];
					var subLength = missedWords.dependents[j].length;
					for (var k = 0; k < subLength; k++) {
						char.push(missedWords.dependents[j].charAt(k));
					}
					
					$scope.notAnswer++;
					$scope.missedWords.push(char);
				}
				
				var lengthDepend = answer.length;
				for (var j = 0; j < lengthDepend; j++) {
					var char = [];
					var subLength = answer[j].length;
					for (var k = 0; k < subLength; k++) {
						char.push(answer[j].charAt(k));
					}
					
					$scope.correctedWords++;
					$scope.completeWords.push(char);
				}
			}
		}
		
		function error(response) {

			$scope.errors.push({
				type : "danger",
				status : response.status,
				statusText : response.statusText,
				message : response.data.message
			});
		}
		
		$scope.closeAlert = function(index) {
			$scope.errors.splice(index, 1);
		};
	}
	controllers.WithDrawnCtrl.$inject = [ '$scope', '$http' ];
	
	controllers.addWordCtrl = function($scope, $http, $timeout){
		$scope.word = {
				name: "",
				relatives: ""
		};
		$scope.errors = [];
		$scope.isLoading =false;
		
		$scope.allWords = [];
		
		$scope.init = function(){
			$http({
				method : 'GET',
				url : '/getAllWords',
				responseType : 'json',
				dataType : 'json',
				headers : {
					'Content-Type' : 'application/json;charset=UTF-8',
					'accept' : 'application/json'
				}
			}).then(function(res){
				if(res.data && res.data.length > 0){
					$scope.allWords = res.data;
				}
			}, error);
		};
		
		$scope.submit = function(){
			if($scope.word.name && $scope.word.relatives){
				$scope.isLoading =true;
				$http({
					method : 'POST',
					url : '/addWord',
					data : JSON.stringify($scope.word),
					responseType : 'json',
					dataType : 'json',
					headers : {
						'Content-Type' : 'application/json;charset=UTF-8',
						'accept' : 'application/json'
					}
				}).then(success, error);
			}
		}
		
		function success(res){
			if(res.data){
				var status = res.data.status;
				if(status != 'OK'){
					$scope.errors.push({
						type : "danger",
						message : "Failed to update! Please try again later."
					});
				}else{
					$scope.word = {
							name: "",
							relatives: ""
					};
					
					if(res.data.words && res.data.words.length > 0){
						$scope.allWords = res.data.words;
					}
					$scope.errors.push({
						type : "success",
						message : "Your data added successfully."
					});
					
				}
			}
			$scope.isLoading =false;
		}
		
		function error(response) {
			$scope.errors.push({
				type : "danger",
				status : response.status,
				statusText : response.statusText,
				message : response.data.message
			});
			$scope.isLoading =false;
		}
		
		$scope.closeAlert = function(index) {
			$scope.errors.splice(index, 1);
		};
		
		$scope.deleteWord = function(word){
			$http({
				method : 'GET',
				url : '/deleteWord/'+word,
				responseType : 'json',
				dataType : 'json',
				headers : {
					'Content-Type' : 'application/json;charset=UTF-8',
					'accept' : 'application/json'
				}
			}).then(function(res){
				if(res.data){
					var status = res.data.status;
					if(status != 'OK'){
						$scope.errors.push({
							type : "danger",
							message : "Failed to delete! Please try again later."
						});
					}
					else if(res.data.list && res.data.list.length > 0){
						$scope.allWords = res.data.list;
					}
				}
			}, error);
		}
	}
	
	controllers.addWordCtrl.$inject = [ '$scope', '$http', '$timeout' ];

	return controllers;

});