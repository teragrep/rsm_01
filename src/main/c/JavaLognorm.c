/*
 * Record Schema Mapping Library for Java RSM-01
 * Copyright (C) 2021-2024 Suomen Kanuuna Oy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Additional permission under GNU Affero General Public License version 3
 * section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with other code, such other code is not for that reason alone subject to any
 * of the requirements of the GNU Affero GPL version 3 as long as this Program
 * is the same Program as licensed from Suomen Kanuuna Oy without any additional
 * modifications.
 *
 * Supplemented terms under GNU Affero General Public License version 3
 * section 7
 *
 * Origin of the software must be attributed to Suomen Kanuuna Oy. Any modified
 * versions must be marked as "Modified version of" The Program.
 *
 * Names of the licensors and authors may not be used for publicity purposes.
 *
 * No rights are granted for use of trade names, trademarks, or service marks
 * which are in The Program if any.
 *
 * Licensee must indemnify licensors and authors for any liability that these
 * contractual assumptions impose on licensors and authors.
 *
 * To the extent this program is licensed as part of the Commercial versions of
 * Teragrep, the applicable Commercial License may apply to this file if you as
 * a licensee so wish it.
 */
#include <stddef.h>
#include <string.h>
#include <liblognorm/liblognorm.h>
#include <liblognorm/lognorm.h>

/* compiler commands for this file, may need additional compiler flags for liblognorm like pcre2 did:
 gcc -c -Wall -Werror -fpic src/main/c/JavaLognorm.c -o src/main/c/JavaLognorm.o -I/usr/include/liblognorm -I/usr/include/json-c
 gcc -shared -Wl,-soname,JavaLognorm.so -o src/main/c/JavaLognorm.so src/main/c/JavaLognorm.o

 Autotools setup commands for this file:
 autoreconf --install
 ./configure
 make

 None of the above matters if maven works as intended and executes the autotools scripts automatically.*/

typedef struct OptionsStruct_TAG {
    int CTXOPT_ALLOW_REGEX;
    int CTXOPT_ADD_EXEC_PATH;
    int CTXOPT_ADD_ORIGINALMSG;
    int CTXOPT_ADD_RULE;
    int CTXOPT_ADD_RULE_LOCATION;
}OptionsStruct;

const char *version() {
    return ln_version();
}

void *initCtx() {
    ln_ctx *ctx = malloc(sizeof(ln_ctx));
    if((*ctx = ln_initCtx()) == NULL) {
        // add exception handling here. ln_ returns null if error occurred.
        return NULL;
    }
    return ctx;
}

void exitCtx(ln_ctx *context) {
    if (*context) {
        ln_exitCtx(*context);
    }
    free(context);
}

void setCtxOpts(ln_ctx *ctx, OptionsStruct *opts) {
    unsigned ctxOpts = 0;
    if (opts->CTXOPT_ALLOW_REGEX != 0) {
        ctxOpts |= LN_CTXOPT_ALLOW_REGEX;
        }
    if (opts->CTXOPT_ADD_EXEC_PATH != 0) {
        ctxOpts |= LN_CTXOPT_ADD_EXEC_PATH;
        }
    if (opts->CTXOPT_ADD_ORIGINALMSG != 0) {
        ctxOpts |= LN_CTXOPT_ADD_ORIGINALMSG;
        }
    if (opts->CTXOPT_ADD_RULE != 0) {
        ctxOpts |= LN_CTXOPT_ADD_RULE;
        }
    if (opts->CTXOPT_ADD_RULE_LOCATION != 0) {
        ctxOpts |= LN_CTXOPT_ADD_RULE_LOCATION;
        }
    ln_setCtxOpts(*ctx, ctxOpts);
}

int loadSamples(ln_ctx *context, char *filename) {
    return ln_loadSamples(*context, filename);
}

int loadSamplesFromString(ln_ctx *context, char *string) {
    return ln_loadSamplesFromString(*context, string);
}

int hasAdvancedStats() {
    return ln_hasAdvancedStats();
}

