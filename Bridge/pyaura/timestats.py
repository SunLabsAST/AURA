
from datetime import datetime
from datetime import timedelta

class TimeStats():

    def __init__(self, total, echo_each=100):
        self.total = total
        self.echo_each = echo_each
        self.i = 0
        self.prevTime = None

        self.rsum = 0.0
        self.rnbr = 0.0

        print "   Progress\t\tTime last\tTime avg\tTime left"

    def next(self):

        self.i += 1

        newTime = datetime.now()

        if self.i%self.echo_each==0:
            if self.prevTime==None:
                self.prevTime = newTime
                print "   %d/%d" % (self.i, self.total)
                
            else:

                tD = newTime - self.prevTime
                self.prevTime = newTime

                tDf = tD.seconds + (tD.microseconds / (10.0 ** len(str(tD.microseconds))))
                iPerSec = self.echo_each / tDf

                self.rsum += iPerSec
                self.rnbr += 1

                nbrPerSec = self.rsum/self.rnbr
                
                left = (self.total-self.i)*nbrPerSec
                delta = timedelta(seconds=int(left))
                #leftStr = ""
                #if delta.days>0:
                #    leftStr += "%d days, " % delta.days
                #if delta.

                print "   %d/%d\t\t%0.4f/sec\t%0.4f/sec\t%0.2fsecs left" % (self.i, self.total, iPerSec, nbrPerSec, (self.total-self.i)/nbrPerSec)

