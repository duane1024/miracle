#!/usr/bin/python

import os
import tidylib
import StringIO
from bs4 import BeautifulSoup

def parseSong(songhtml):
	tidyhtml, errors = tidylib.tidy_document(songhtml)
	song = BeautifulSoup(tidyhtml, "lxml")
	title = song.find('div', class_='gumax-firstHeading').text.strip()
	
	lyricsanchor = song.find('a', id='Lyrics')
	lyrics = lyricsanchor.parent
	rawtext = lyrics.get_text()
	textio = StringIO.StringIO(rawtext)
	
	songtext = ''
	for line in textio.readlines():
		line = line.strip()
		if (line.lower() == 'lyrics'):
			continue
		else:
			songtext += line + os.linesep
			
	# strip leading and trailing spaces
	songtext = songtext.strip()
			
	print "Printing %s:\n" % title
	print songtext

songdir = "../data/queenpedia/"

for filename in os.listdir(songdir):
	if not (filename.endswith('html')):
		continue
	print "Parsing %s" % filename
	songhtml = ''
	for line in (open(songdir + os.sep + filename, 'r')).readlines():
		songhtml += line
	parseSong(songhtml)
	