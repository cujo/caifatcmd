/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <string.h>
#include <stdio.h>
#include <errno.h>
#include <jni.h>
#include <sys/socket.h>
#include <sys/time.h>
#include "caif_socket.h"
#include "atchannel.h"
#include "at_tok.h"

#ifndef AF_CAIF
#define AF_CAIF		37
#endif
#ifndef SOL_CAIF
#define SOL_CAIF	278
#endif
#define MAX_AT_RESPONSE (8 * 1024)

// AT socket FD
int fd;

int initATIntf(void) {
	int err;
	struct sockaddr_caif addr;
	struct timeval tout;
	addr.family = AF_CAIF;
	addr.u.at.type = CAIF_ATTYPE_PLAIN;
	tout.tv_sec = 5;
	tout.tv_usec = 0;

	fd = socket(AF_CAIF, SOCK_SEQPACKET, CAIFPROTO_AT);

	if (fd == -1)
		err = errno;
	else
		err = 0;

	if (!err) {
		err = connect(fd, (const struct sockaddr *)&addr, sizeof(addr));
		if (err == -1)
			err = errno;
		else
			err = 0;
	}

	return err;
}

// JNI call
jstring
Java_com_github_caifatcmd_MainActivity_initATIntf( JNIEnv* env,
                                                  jobject thiz )
{
	char *err = "NO error";
	int error;

	error = initATIntf();
	if (error)
		err = strerror(error);

    return (*env)->NewStringUTF(env, err);
}

// Exe call
int main (int argc, char **argv) {
	char *err = "No error", at_cmd[256];
	int error, vol;
	ATResponse *atresp = NULL;

	if (argc > 1) {
		vol = atoi(argv[1]);
	} else {
		vol = 8;
	}

	if (vol == 0)
		sprintf(at_cmd, "AT*EADVOL?");
	else
		sprintf(at_cmd, "AT*EADVOL=-%d", vol);

	//printf("argc = %d, cmd = %s\n", argc, at_cmd);

	error = initATIntf();

	if (error)
		err = strerror(error);
	//printf("FD open status: %s (%d, %d)\n", err, error, fd);

	if (error)
		close(fd);
	else {
		int ret;
		error = at_open(fd, NULL);
		//at_handshake();
		ret = at_send_command_multiline(at_cmd, "*EADVOL", &atresp);
		if (ret < 0 || atresp->success == 0) {
			printf("ERROR\n");
		} else {
			ATLine *atline;
			atline = atresp->p_intermediates;
			printf("OK\n");
			while (atline) {
				printf("RX: %s\n", atline->line);
				atline = atline->p_next;
			}
		}
		if (atresp != NULL)
			at_response_free(atresp);
	}

	return error;
}
