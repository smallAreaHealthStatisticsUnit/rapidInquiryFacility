#
# Function:     rif40_capture.output()
# Parameters:   expression to be executed, function name, warnings are errors (TRUE/[FALSE])
# Returns:      Nothing
# Description:  Run expression, capturing output
#
rif40_capture.output<-function(..., function_name="Unknown", warnings_are_errors=FALSE) {
        tryCatch(
                {
                        rval=""
                        args=substitute(list(...))[-1L]
                        rval=capture.output(
								eval(
									parse(text=toString(args))), 
										file = NULL, append = FALSE)
						if (is.null(rval) != TRUE) rval=""
                        if (nchar(rval > 0)) {
                                rif40_log("DEBUG1", f,
                                        sprintf("%s >>>\n%s\n<<<",
                                                toString(args), rval))
                        }
                },
                error=function(capture_error) {
                        rif40_error(-90156, function_name,
                                sprintf("rif40_capture.output>>>\n%s\n<<<\nError: %s",
                                        toString(args), capture_error))
                },
                warning=function(capture_warning) {
                        if (warnings_are_errors) {
                                rif40_error(-90130, function_name,
                                        sprintf("rif40_capture.output>>>\n%s\n<<<\nWarning: %s",
                                                toString(args), capture_warning))
                        }
                        else {
                                rif40_log("WARNING", function_name,
                                        sprintf("rif40_capture.output>>>\n%s\n<<<\nWarning: %s",
                                                toString(args), capture_warning))
                        }
                }
        )       # End of TryCatch()
}
#
# Eof