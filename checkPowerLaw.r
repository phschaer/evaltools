freq <- c(25,8,5,4,3,3,3,3,2,2,2,2,2,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1)
# x <- (1-runif(10000))^(-1/(2.5-1))
index <- 1:length(freq)
plot(index,freq,log="yx",ylab="frequency",xlab="rank")
abline(lm(log10(x)~log10(y)))
plfit(freq)

plfit(x)
[1] "(plfit) Warning : finite-size bias may be present"
$xmin
[1] 2

$alpha
[1] 2.35

$D
[1] 0.05350649

