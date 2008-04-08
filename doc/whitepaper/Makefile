latex=pdflatex
bibtex=bibtex
title = aura_whitepaper

pid=`echo $$PPID`

quik:
	${latex} ${title} 
#	make backup

final:
	${bibtex} ${title}
	${latex} ${title} 
	${latex} ${title} 

backup:
	@tar cf jnmr.tar *.tex
	@cp jnmr.tar bu/${pid}.jnmr.tar
clean:
	rm -f ${title}.dvi
	rm -f ${title}.ps
	rm -f ${title}.log
	rm -f ${title}.bbl
	rm -f ${title}.aux
	rm -f ${title}.blg
