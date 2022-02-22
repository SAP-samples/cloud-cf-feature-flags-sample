package errors

import (
	"errors"
	"log"
)

func Check(err error) {
	if err != nil {
		log.Fatalln(err.Error())
	}
}

var New = errors.New
