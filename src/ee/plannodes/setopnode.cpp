/* This file is part of VoltDB.
 * Copyright (C) 2008-2016 VoltDB Inc.
 *
 * This file contains original code and/or modifications of original code.
 * Any modifications made by VoltDB Inc. are licensed under the following
 * terms and conditions:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */
/* Copyright (C) 2008 by H-Store Project
 * Brown University
 * Massachusetts Institute of Technology
 * Yale University
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
#include "setopnode.h"

#include "common/SerializableEEException.h"

#include <sstream>

namespace voltdb {

SetOpPlanNode::~SetOpPlanNode() { }

PlanNodeType SetOpPlanNode::getPlanNodeType() const { return PLAN_NODE_TYPE_SETOP; }

std::string SetOpPlanNode::debugInfo(const std::string &spacer) const
{
    std::ostringstream buffer;
    buffer << spacer << "SetOpType[" << m_setopType << "]\n";
    return buffer.str();
}

void SetOpPlanNode::loadFromJSONObject(PlannerDomValue obj)
{
    std::string setopTypeStr = obj.valueForKey("SETOP_TYPE").asStr();
    if (setopTypeStr == "UNION") {
        m_setopType = SETOP_TYPE_UNION;
    } else if (setopTypeStr == "UNION_ALL") {
        m_setopType = SETOP_TYPE_UNION_ALL;
    } else if (setopTypeStr == "INTERSECT") {
        m_setopType = SETOP_TYPE_INTERSECT;
    } else if (setopTypeStr == "INTERSECT_ALL") {
        m_setopType = SETOP_TYPE_INTERSECT_ALL;
    } else if (setopTypeStr == "EXCEPT") {
        m_setopType = SETOP_TYPE_EXCEPT;
    } else if (setopTypeStr == "EXCEPT_ALL") {
        m_setopType = SETOP_TYPE_EXCEPT_ALL;
    } else if (setopTypeStr == "NONE") {
        m_setopType = SETOP_TYPE_NONE;
    } else {
        throw SerializableEEException(VOLT_EE_EXCEPTION_TYPE_EEEXCEPTION,
                                      "SetopPlanNode::loadFromJSONObject:"
                                      " Unsupported SETOP_TYPE value " +
                                      setopTypeStr);
    }
}

} // namespace voltdb