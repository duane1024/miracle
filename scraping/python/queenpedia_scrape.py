#!/usr/bin/python

import os
import urllib2
import urlparse
import tidylib
from bs4 import BeautifulSoup

def parseSongs(songitems):
	localsongs = []
	for songitem in songitems:
		if (songitem.a):
			songname = songitem.a.string
			songurl = songitem.a.get('href')
			songtuple = songname, songurl
			localsongs.append(songtuple)
	return localsongs

def downloadSong(songtitle, songurl):
	songhtml = ''
	basedir = "../data/queenpedia/"
	fileext = ".html"

	if not os.path.exists(basedir):
		os.makedirs(basedir)
	
	for line in (urllib2.urlopen(songurl)).readlines():
		songhtml += line
	songfilename = "".join([x if x.isalnum() else "_" for x in songtitle]) 
	songfile = basedir + songfilename + fileext
	uniq = 1
	while os.path.exists(songfile):
		songfile = '%s%s_%d%s' % (basedir, songfilename, uniq, fileext)
		uniq += 1

	print "Downloading Song: " + songtitle + " as " + songfile
	with open(songfile, 'w') as f:
		f.write(songhtml)

	#songsoup = BeautifulSoup(songhtml, "lxml")
	#print songsoup

def downloadContents(url):
	htmlcontents = ''
	for line in (urllib2.urlopen(url)).readlines():
		htmlcontents += line

	songs = BeautifulSoup(htmlcontents, "lxml")
	print "Title: " + songs.title.string

	bodycontent = songs.find(id="bodyContent")

	# allsongs will be a list containing tuples with form: (url, songname)
	allsongs = []

	# find table elements directly below the bodyContent div tag
	tables = bodycontent.find_all('table', recursive=False)
	# remove the first table; this is the table of contents
	tables.pop(0)
	# find the list elements embedded in these tables
	for table in tables:
		tds = table.tr.find_all('td')
		for td in tds:
			songlist = td.ul

			songitems = songlist.find_all('li')

			parsedSongs = parseSongs(songitems)
			allsongs.extend(parsedSongs)

	# now find the lists that are direct descendants of bodyContent div tag
	# these songs are formatted in the html differently than the others above for some reason
	uls = bodycontent.find_all('ul', recursive=False)
	for ul in uls:
		songitems = ul.find_all('li')

		parsedSongs = parseSongs(songitems)
		allsongs.extend(parsedSongs)
	
	return allsongs

# for debugging if we want to only deal with local file system
#htmlfile = '/Users/ddmoore/dev/miracle/scratch/queenpedia.html'
#songsoup = BeautifulSoup(open(htmlfile))

mainurl = 'http://queenpedia.com/index.php?title=Song_List'
songs = downloadContents(mainurl)

print len(songs)

for song in songs:
	songtitle = song[0].strip()
	#print "Song Title: " + songtitle
	songurl = urlparse.urljoin(mainurl, song[1])
	#print "URL: " + songurl
	downloadSong(songtitle, songurl)


