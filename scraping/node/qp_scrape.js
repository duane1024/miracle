
var $ = require('jquery');
var http = require('http');

var queenpediaSongList = "http://queenpedia.com/index.php?title=Song_List";

var html = '';
http.get(queenpediaSongList, function(result) {
	result.on('data', function(data) {
		html += data;
	}).on('end', function() {
		// remove the first table as this is the table of contents
		// the 'tables' object returned from jquery's find() method is not a real Array
		// so we can't do pop() but it does implement slice()
		var songitemsInTables = $(html).find('#bodyContent > table').slice(1).find('td').find('li');		
		var songitemsInList = $(html).find('#bodyContent > ul').find('li');
		var songitems = $.merge(songitemsInTables, songitemsInList);
		
		console.log('Found ' + songitems.length + ' songs');
		
		songitems.each(function() {
			var songtitle = $(this).find('a').text().trim();
			var songurl = $(this).find('a').attr('href');
			console.log("Song Title = " + songtitle + ", Song URL = " + songurl);
		});
	});
});
