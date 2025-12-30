#version 150

#define _Speed 3.0  //disk rotation speed

#define _Steps  12. //disk texture layers
#define _Size 0.3 //size of BH

uniform float GameTime;
uniform mat4 InvViewMat;

in vec3 rayDirection;
in vec3 rayOrigin;
in vec2 texCoord;

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

out vec4 fragColor;

//layout (std140) uniform BlackHoleList
//{
//    int numBlackHoles;
//    BlackHole holes[];
//};

float hash(float x){ return fract(sin(x)*152754.742);}
float hash(vec2 x){	return hash(x.x + hash(x.y));}

float value(vec2 p, float f) //value noise
{
    float bl = hash(floor(p*f + vec2(0.,0.)));
    float br = hash(floor(p*f + vec2(1.,0.)));
    float tl = hash(floor(p*f + vec2(0.,1.)));
    float tr = hash(floor(p*f + vec2(1.,1.)));

    vec2 fr = fract(p*f);
    fr = (3. - 2.*fr)*fr*fr;
    float b = mix(bl, br, fr.x);
    float t = mix(tl, tr, fr.x);
    return  mix(b,t, fr.y);
}

vec4 background(vec3 ray)
{
    vec2 uv = ray.xy;

    if( abs(ray.x) > 0.5)
    uv.x = ray.z;
    else if( abs(ray.y) > 0.5)
    uv.y = ray.z;

    //vec4 nebulae = texture(DiffuseSampler, (uv*1.5 ));
    vec4 nebulae = texture(DiffuseSampler, (texCoord ));

    return nebulae;
}

vec4 raymarchDisk(vec3 ray, vec3 zeroPos)
{
    //return vec4(1.,1.,1.,0.); //no disk

    vec3 position = zeroPos;
    float lengthPos = length(position.xz);
    float invSize = 1.0 / _Size;
    float invSteps = 1.0 / _Steps;
    float invAbsRayY = 1.0 / max(abs(ray.y), 1e-4);
    float dist = min(1., lengthPos * invSize * 0.5) * _Size * 0.4 * invSteps * invAbsRayY;

    position += dist*_Steps*ray*0.5;

    vec2 deltaPos;
    deltaPos.x = -zeroPos.z*0.01 + zeroPos.x;
    deltaPos.y = zeroPos.x*0.01 + zeroPos.z;
    deltaPos = normalize(deltaPos - zeroPos.xz);

    float parallel = dot(ray.xz, deltaPos);
    parallel /= sqrt(lengthPos);
    parallel *= 0.5;
    float redShift = parallel +0.3;
    redShift *= redShift;

    redShift = clamp(redShift, 0., 1.);

    float disMix = clamp((lengthPos - _Size * 2.) * invSize * 0.24, 0., 1.);
    vec3 insideCol =  mix(vec3(1.0,0.8,0.0), vec3(0.5,0.13,0.02)*0.2, disMix);

    insideCol *= mix(vec3(0.4, 0.2, 0.1), vec3(1.6, 2.4, 4.0), redShift);
    insideCol *= 1.25;
    redShift += 0.12;
    redShift *= redShift;

    vec4 o = vec4(0.);

    float rot = mod(GameTime*_Speed, 8192.);
    float sinRot = sin(rot);
    float cosRot = cos(rot);
    float noiseScale = invSize * 0.05;
    float intensityBase = 2.0 * invSteps;
    float widthFactor = invSize * 10.0 + 0.01;

    for(float i = 0. ; i < _Steps; i++)
    {
        position -= dist * ray ;

        float intensity = clamp(1.0 - abs((i - 0.8) * intensityBase), 0., 1.);
        float lengthPos = length(position.xz);
        float distMult = 1.;

        float normalizedLength = lengthPos * invSize;
        distMult *= clamp((normalizedLength - 0.75) * 1.5, 0., 1.);
        distMult *= clamp((10.0 - normalizedLength) * 0.20, 0., 1.);
        distMult *= distMult;

        float u = lengthPos + GameTime * _Size * 0.3 + intensity * _Size * 0.2;

        vec2 xy ;
        xy.x = -position.z * sinRot + position.x * cosRot;
        xy.y = position.x * sinRot + position.z * cosRot;

        float x = abs(xy.x / max(abs(xy.y), 1e-4));
        float angle = 0.02 * atan(x);

        const float f = 70.;
        vec2 noiseCoord = vec2(angle, u * noiseScale);
        float noise = value(noiseCoord, f);
        float noiseHi = value(noiseCoord, f * 2.);
        noise = noise * 0.66 + 0.33 * noiseHi;

        float extraWidth = noise * (1.0 - clamp(i * invSteps * 2.0 - 1.0, 0., 1.));

        float alpha = clamp(noise * (intensity + extraWidth) * widthFactor *  dist * distMult , 0., 1.);

        vec3 col = 2.*mix(vec3(0.3,0.2,0.15)*insideCol, insideCol, min(1.,intensity*2.));
        o = clamp(vec4(col*alpha + o.rgb*(1.-alpha), o.a*(1.-alpha) + alpha), vec4(0.), vec4(1.));

        float scaledLength = max(normalizedLength * normalizedLength, 1e-4);

        o.rgb += redShift * (intensity + 0.5) * invSteps * 100. * distMult / scaledLength;
    }

    o.rgb = clamp(o.rgb - 0.005, 0., 1.);
    return o ;
}


void Rotate( inout vec3 vector, vec2 angle )
{
    vector.yz = cos(angle.y)*vector.yz
    +sin(angle.y)*vec2(-1,1)*vector.zy;
    vector.xz = cos(angle.x)*vector.xz
    +sin(angle.x)*vec2(-1,1)*vector.zx;
}

vec3 transformRay(mat4 mat, vec3 ray)
{
    return (mat * vec4(ray,1)).xyz;
}

mat4 translate(mat4 mat, vec3 translation)
{
    mat[3].xyz += translation;
    return mat;
}

bool raySphere(vec3 ro, vec3 rd, vec3 center, float radius, out float t, out vec3 hitPos)
{
    vec3 oc = ro - center;
    float b = dot(oc, rd);
    float c = dot(oc, oc) - radius * radius;
    float discriminant = b * b - c;
    if(discriminant < 0.0)
    {
        t = 0.0;
        hitPos = vec3(0.0);
        return false;
    }

    float s = sqrt(discriminant);
    float t0 = -b - s;
    float t1 = -b + s;
    t = (t0 > 0.0) ? t0 : t1;
    if(t < 0.0)
    {
        hitPos = vec3(0.0);
        return false;
    }
    hitPos = ro + rd * t;
    return true;
}


uniform vec3 BlackHolePosition;
uniform float Near;
uniform float Far;

float linearizeDepth(float depth, float near, float far) {
    float ndc = depth * 2.0 - 1.0;
    return (2.0 * near * far) / (far + near - ndc * (far - near));
}

void main(  )
{
 //   colOut = vec4(0.);;


    //setting up camera
    vec3 ray = normalize(rayDirection);
    ray = transformRay(InvViewMat, ray);
    vec3 startRay = ray;
    // ray = normalize(vec3(0, -1, 0));
    vec3 pos = rayOrigin - BlackHolePosition;
    vec3 baseScene = texture(DiffuseSampler, texCoord).rgb;

    // Sample scene depth for occlusion
    float sceneDepthRaw = texture(DepthSampler, texCoord).r;
    float sceneDepth = linearizeDepth(sceneDepthRaw, Near, Far);
    float blackHoleDist = length(BlackHolePosition - rayOrigin);

    vec4 col = vec4(0.);
    vec4 glow = vec4(0.);
    vec4 outCol =vec4(100.);
    float invSizeMain = 1.0 / _Size;
    float bendScale = _Size * 0.625;
    float closeLimitCoeff = 0.05 * invSizeMain;
    float diskHeight = _Size * 0.002;
    float diskAdvance = _Size * 0.001;
    float nearThreshold = _Size * 0.1;
    float farThreshold = _Size * 1000.;

    mat3 viewMat = transpose(mat3(InvViewMat));
    vec3 rayView = normalize(viewMat * startRay);
    vec3 holeCenterView = viewMat * BlackHolePosition;

    vec3 lensSample = baseScene;
    float lensBlend = 0.0;
    if(holeCenterView.z < -0.01)
    {
        float lensRadius = _Size * 2.8;
        float lensInnerRadius = _Size * 1.1;
        float tHit;
        vec3 hitPos;
        if(raySphere(vec3(0.0), rayView, holeCenterView, lensRadius, tHit, hitPos))
        {
            vec3 normalView = normalize(hitPos - holeCenterView);
            float viewDot = clamp(dot(rayView, normalView), -1.0, 1.0);
            float radial = sqrt(max(1.0 - viewDot * viewDot, 0.0));
            float fresnel = 1.0 - radial;
            float fresnelSq = fresnel * fresnel;
            float distToHole = length(holeCenterView);
            float effectFraction = lensRadius / max(distToHole, 0.001);

            vec2 centerProj = holeCenterView.xy / max(-holeCenterView.z, 0.001);
            vec2 hitProj = hitPos.xy / max(-hitPos.z, 0.001);
            vec2 dirVec = hitProj - centerProj;
            float dirLen = length(dirVec);

            if(dirLen > 1e-4)
            {
                vec2 dir = dirVec / dirLen;
                float shellDist = length(hitPos - holeCenterView);
                float proximity = 1.0 - smoothstep(lensInnerRadius, lensRadius, shellDist);
                float offsetMag = 0.35 * effectFraction * fresnelSq * proximity;
                vec2 newUV = texCoord + dir * offsetMag;
                newUV = clamp(newUV, vec2(0.0), vec2(1.0));
                lensSample = texture(DiffuseSampler, newUV).rgb;
                lensBlend = clamp(fresnelSq * proximity, 0.0, 1.0);
            }
        }
    }

    for(int disks = 0; disks< 20; disks++) //steps
    {

        for (int h = 0; h < 6; h++) //reduces tests for exit conditions (to minimise branching)
        {
            float dotpos = dot(pos,pos);
            float invDist = inversesqrt(dotpos); //1/distance to BH
            float centDist = dotpos * invDist; 	//distance to BH
            float invRayY = 1.0 / max(abs(ray.y), 1e-4);
            float stepDist = 0.92 * abs(pos.y) * invRayY;  //conservative distance to disk (y==0)
            float farLimit = centDist * 0.5; //limit step size far from to BH
            float closeLimit = centDist * 0.1 + closeLimitCoeff * centDist * centDist; //limit step size close to BH
            stepDist = min(stepDist, min(farLimit, closeLimit));

            float invDistSqr = invDist * invDist;
            float bendForce = stepDist * invDistSqr * bendScale;  //bending force
            ray =  normalize(ray - (bendForce * invDist )*pos);  //bend ray towards BH
            pos += stepDist * ray;

            glow += vec4(1.2,1.1,1, 1.0) *(0.01*stepDist * invDistSqr * invDistSqr *clamp( centDist*(2.) - 1.2,0.,1.)); //adds fairly cheap glow
        }

        float distToCenter = length(pos);

        if(distToCenter < nearThreshold) //ray sucked in to BH
        {
            outCol =  vec4( col.rgb * col.a + glow.rgb *(1.-col.a ) ,1.) ;
            break;
        }

        else if(distToCenter > farThreshold) //ray escaped BH
        {
            vec3 finalRay = ray;
            vec3 lensScene = mix(baseScene, lensSample, lensBlend);

            vec4 bg = background (finalRay);
            vec3 finalScene = mix(bg.rgb, lensScene, 0.85);
            outCol = vec4(col.rgb*col.a + finalScene*(1.-col.a)  + glow.rgb *(1.-col.a    ), 1.);
            break;
        }

        else if (abs(pos.y) <= diskHeight ) //ray hit accretion disk
        {
            vec4 diskCol = raymarchDisk(ray, pos);   //render disk
            pos.y = 0.;
            float safeRayY = max(abs(ray.y), 1e-4);
            pos += (diskAdvance / safeRayY) * ray;
            col = vec4(diskCol.rgb*(1.-col.a) + col.rgb, col.a + diskCol.a*(1.-col.a));
        }
    }

    //if the ray never escaped or got sucked in
    if(outCol.r == 100.)
    outCol = vec4(col.rgb + glow.rgb *(col.a +  glow.a) , 1.);

    col = outCol;
   // col.rgb =  pow( col.rgb, vec3(0.6) );

    // Per-pixel depth occlusion - hide black hole behind geometry
    if (sceneDepth < blackHoleDist) {
        fragColor = vec4(baseScene, 1.0);
    } else {
        fragColor = col;
    }

}
